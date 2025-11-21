"""
피드백 요청 API 성능 테스트 스크립트 (병렬 버전)
- 파일 크기별로 병렬 호출
- 스레드별 requests.Session 재사용(커넥션 풀)
- 결과 CSV 저장 및 요약 출력
"""

import os
import time
import csv
import threading
import statistics
from datetime import datetime
from io import BytesIO
from concurrent.futures import ThreadPoolExecutor, as_completed

import requests
from requests.adapters import HTTPAdapter

# ============== 설정 ==============
BASE_URL = "http://localhost:8080/v1/expert-applications"
EXPERT_ID = 6                 # Path Variable
BUSINESS_PLAN_ID = 2          # Request Param

# 인증 토큰 (JWT) - 환경변수 권장: export AUTH_TOKEN="xxxxx"
AUTH_TOKEN = ""

# 테스트할 파일 크기 (MB)
FILE_SIZES_MB = [5, 10, 15, 20]

# 각 크기별 반복 횟수
ITERATIONS_PER_SIZE = 10

# 크기별 동시 실행 스레드 수 (예: 5). 과도하면 서버를 과부하시킬 수 있음.
CONCURRENCY_PER_SIZE = 5

# 요청 타임아웃(초)
REQUEST_TIMEOUT = None

# (선택) 워밍업 요청 개수: 첫 연결/템플릿 로딩 등 콜드스타트 흡수
WARMUP_REQUESTS = 0
# ===================================

# 스레드별 세션 보관소
_thread_local = threading.local()
# 출력 동기화용
_print_lock = threading.Lock()


def get_session() -> requests.Session:
    """
    스레드별 Session을 생성/재사용.
    커넥션 풀을 늘려 다중 연결 병렬성을 확보.
    """
    sess = getattr(_thread_local, "session", None)
    if sess is None:
        sess = requests.Session()
        # 풀 크기는 동시성보다 여유 있게
        pool_size = max(10, CONCURRENCY_PER_SIZE * 2)
        adapter = HTTPAdapter(pool_connections=pool_size, pool_maxsize=pool_size)
        sess.mount("http://", adapter)
        sess.mount("https://", adapter)
        _thread_local.session = sess
    return sess


def create_dummy_pdf_bytes(size_mb: int) -> bytes:
    """
    더미 PDF 바이트 생성 (헤더 + 패딩)
    매 호출 시 BytesIO를 새로 감싸 쓰되 원본 바이트는 캐시해 재사용.
    """
    pdf_header = b"%PDF-1.4\n"
    size_bytes = size_mb * 1024 * 1024
    if size_bytes < len(pdf_header):
        size_bytes = len(pdf_header)
    return pdf_header + (b"0" * (size_bytes - len(pdf_header)))


def send_feedback_request(file_bytes: bytes, file_size_mb: int):
    """
    피드백 요청 API 호출 (단일 요청)
    """
    try:
        # 각 요청마다 새로운 BytesIO(스트림 포지션 충돌 방지)
        file_obj = BytesIO(file_bytes)

        url = f"{BASE_URL}/{EXPERT_ID}/request"
        files = {"file": (f"test_{file_size_mb}MB.pdf", file_obj, "application/pdf")}
        params = {"businessPlanId": BUSINESS_PLAN_ID}
        headers = {}
        if AUTH_TOKEN:
            headers["Authorization"] = f"Bearer {AUTH_TOKEN}"

        sess = get_session()
        start = time.perf_counter()
        resp = sess.post(url, files=files, params=params, headers=headers, timeout=REQUEST_TIMEOUT)
        elapsed_ms = (time.perf_counter() - start) * 1000.0

        success = (resp.status_code == 200)
        error_msg = None if success else (resp.text[:200] if resp.text else f"HTTP {resp.status_code}")
        return success, elapsed_ms, resp.status_code, error_msg

    except requests.exceptions.Timeout:
        return False, None, None, "Timeout"
    except Exception as e:
        return False, None, None, str(e)[:200]


def run_test_parallel():
    print("=" * 70)
    print("📊 피드백 요청 API 성능 테스트 (병렬)")
    print("=" * 70)
    print(f"API URL: {BASE_URL}/{EXPERT_ID}/request")
    print(f"Expert ID: {EXPERT_ID} (Path Variable)")
    print(f"Business Plan ID: {BUSINESS_PLAN_ID} (Request Param)")
    print(f"테스트 파일 크기: {FILE_SIZES_MB} MB")
    print(f"각 크기별 반복 횟수: {ITERATIONS_PER_SIZE}")
    print(f"크기별 동시성: {CONCURRENCY_PER_SIZE}")
    print("=" * 70)

    # 결과 저장
    all_results = []

    # 크기별 워밍업 (옵션)
    if WARMUP_REQUESTS > 0:
        print("\n🔥 워밍업 시작...")
        fb = create_dummy_pdf_bytes(FILE_SIZES_MB[0])
        for _ in range(WARMUP_REQUESTS):
            send_feedback_request(fb, FILE_SIZES_MB[0])
        print("🔥 워밍업 종료")

    for size_mb in FILE_SIZES_MB:
        print(f"\n🔍 [{size_mb}MB 파일] 병렬 테스트 시작")
        print("-" * 70)

        file_bytes = create_dummy_pdf_bytes(size_mb)
        size_results_times = []
        success_count = 0

        futures = []
        with ThreadPoolExecutor(max_workers=CONCURRENCY_PER_SIZE) as ex:
            for i in range(1, ITERATIONS_PER_SIZE + 1):
                futures.append(ex.submit(send_feedback_request, file_bytes, size_mb))

            for idx, fut in enumerate(as_completed(futures), start=1):
                success, elapsed_ms, status_code, err = fut.result()

                # 결과 집계
                if success and elapsed_ms is not None:
                    success_count += 1
                    size_results_times.append(elapsed_ms)

                # 결과 저장(행 단위)
                all_results.append({
                    "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
                    "file_size_mb": size_mb,
                    "iteration": idx,
                    "success": success,
                    "response_time_ms": round(elapsed_ms, 2) if elapsed_ms is not None else None,
                    "status_code": status_code,
                    "error": err
                })

                # 진행 출력은 락으로 깔끔하게
                with _print_lock:
                    if success:
                        print(f"  ✅ [{idx}/{ITERATIONS_PER_SIZE}] {elapsed_ms:.2f} ms (HTTP {status_code})")
                    else:
                        print(f"  ❌ [{idx}/{ITERATIONS_PER_SIZE}] {err}")

        # 크기별 통계
        if size_results_times:
            avg_time = statistics.mean(size_results_times)
            min_time = min(size_results_times)
            max_time = max(size_results_times)
            success_rate = (success_count / ITERATIONS_PER_SIZE) * 100
            print(f"\n📈 [{size_mb}MB] 결과")
            print(f"  성공률: {success_rate:.1f}% ({success_count}/{ITERATIONS_PER_SIZE})")
            print(f"  평균: {avg_time:.2f} ms | 최소: {min_time:.2f} ms | 최대: {max_time:.2f} ms")
        else:
            print(f"\n❌ [{size_mb}MB] 모든 요청 실패")

    save_results_to_csv(all_results)
    print_summary(all_results)


def save_results_to_csv(results):
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = f"feedback_test_results_{timestamp}.csv"
    with open(filename, "w", newline="", encoding="utf-8") as f:
        fieldnames = ["timestamp", "file_size_mb", "iteration", "success",
                      "response_time_ms", "status_code", "error"]
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(results)
    print(f"\n💾 결과가 저장되었습니다: {filename}")


def print_summary(all_results):
    print("\n" + "=" * 70)
    print("📊 전체 테스트 요약")
    print("=" * 70)

    # 크기별 그룹핑
    by_size = {}
    for r in all_results:
        size = r["file_size_mb"]
        if size not in by_size:
            by_size[size] = []
        if r["success"] and r["response_time_ms"] is not None:
            by_size[size].append(r["response_time_ms"])

    # 표 출력
    print(f"\n{'파일크기':<10} {'성공률':<10} {'평균(ms)':<12} {'최소(ms)':<12} {'최대(ms)':<12}")
    print("-" * 70)
    total_success = 0
    for size_mb in FILE_SIZES_MB:
        times = by_size.get(size_mb, [])
        if times:
            success_rate = (len(times) / ITERATIONS_PER_SIZE) * 100
            avg_t = statistics.mean(times)
            min_t = min(times)
            max_t = max(times)
            total_success += len(times)
            print(f"{size_mb}MB{'':<6} {success_rate:.1f}%{'':<5} "
                  f"{avg_t:.2f}{'':<6} {min_t:.2f}{'':<6} {max_t:.2f}")
        else:
            print(f"{size_mb}MB{'':<6} {'0.0%':<10} {'-':<12} {'-':<12} {'-':<12}")

    total_requests = len(FILE_SIZES_MB) * ITERATIONS_PER_SIZE
    if total_success > 0:
        all_times = [t for v in by_size.values() for t in v]
        overall_avg = statistics.mean(all_times)
        print(f"\n전체 성공률: {(total_success/total_requests)*100:.1f}% "
              f"({total_success}/{total_requests})")
        print(f"전체 평균 응답시간: {overall_avg:.2f} ms")
    print()


if __name__ == "__main__":
    try:
        run_test_parallel()
    except KeyboardInterrupt:
        print("\n\n⚠️  테스트가 중단되었습니다.")
    except Exception as e:
        print(f"\n\n❌ 오류 발생: {e}")
        import traceback
        traceback.print_exc()
