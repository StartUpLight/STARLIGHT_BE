package starlight.adapter.ncp.ocr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import starlight.shared.dto.ClovaOcrResponse;
import starlight.adapter.ncp.ocr.exception.OcrException;
import starlight.adapter.ncp.ocr.infra.ClovaOcrClient;
import starlight.adapter.ncp.ocr.util.OcrResponseMerger;
import starlight.adapter.ncp.ocr.util.OcrTextExtractor;
import starlight.adapter.ncp.ocr.util.PdfUtils;
import starlight.adapter.ncp.ocr.infra.PdfDownloadClient;
import starlight.application.infrastructure.provided.OcrProvider;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClovaOcrProvider implements OcrProvider {

    private static final int MAX_PAGES_PER_REQUEST = 10;

    private final ClovaOcrClient clovaOcrClient;
    private final PdfDownloadClient pdfDownloadClient;

    /**
     * 지정한 PDF URL을 전체 페이지 OCR 처리한 뒤, 단일 응답으로 병합해 반환한다.
     * 1) PDF 다운로드 → 2) 10페이지씩 분할 → 3) 각 조각별 OCR 호출 → 4) 전체 응답 병합
     *
     * @param pdfUrl 접근 가능한 원격 PDF의 절대 URL
     * @return 병합된 CLOVA OCR 응답
     * @throws OcrException 다음 에러 타입으로 래핑되어 발생할 수 있음
     *         - {@code PDF_DOWNLOAD_ERROR} : 네트워크/HTTP 오류 등으로 PDF 다운로드 실패
     *         - {@code PDF_EMPTY_RESPONSE} : 응답 본문이 비어 있음
     *         - {@code PDF_TOO_LARGE}      : 허용된 최대 크기를 초과
     *         - {@code PDF_SPLIT_ERROR}    : PDF 분할 실패
     *         - {@code OCR_TIMEOUT}        : OCR 호출 타임아웃/네트워크 실패
     *         - {@code OCR_CLIENT_ERROR}   : OCR 서버에서 4xx/5xx 등 오류 응답
     */
    @Override
    public ClovaOcrResponse ocrPdfByUrl(String pdfUrl) {
        byte[] pdfBytes = pdfDownloadClient.downloadPdfFromUrl(pdfUrl);

        List<byte[]> chunks = PdfUtils.splitByPageLimit(pdfBytes, MAX_PAGES_PER_REQUEST);

        List<ClovaOcrResponse> parts = new ArrayList<>();
        for (byte[] chunk : chunks) {
            parts.add(clovaOcrClient.recognizePdfBytes(chunk));
        }

        return OcrResponseMerger.merge(parts);
    }

    /**
     * 한 번에 텍스트 완성본까지 반환하는 편의 메서드.
     * - 위의 ocrPdfByUrl로 병합 응답을 만든 뒤,
     * - OcrTextExtractor로 토큰을 정제/라인브레이크 반영하여 평문 텍스트를 생성.
     *
     * @param pdfUrl         원격 PDF URL
     * @param minConfidence  신뢰도 하한 (이 값보다 낮은 토큰은 제거)
     * @return 페이지 구분선(“-----”)이 포함된 최종 텍스트
     */
    @Override
    public String ocrPdfTextByUrl(String pdfUrl, double minConfidence) {
        ClovaOcrResponse clovaOcrResponse = ocrPdfByUrl(pdfUrl);
        return OcrTextExtractor.toPlainText(clovaOcrResponse, minConfidence);
    }
}
