package starlight.adapter.aireport.infrastructure.ocr.infra;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import starlight.adapter.aireport.infrastructure.ocr.exception.OcrErrorType;
import starlight.adapter.aireport.infrastructure.ocr.exception.OcrException;

import java.net.URI;

@Slf4j
@Component
public class PdfDownloadClient {

    private static final int MAX_PDF_BYTES = 30 * 1024 * 1024; // 30MB까지 허용

    private final RestClient pdfDownloadClient;

    public PdfDownloadClient(@Qualifier("pdfDownloadRestClient") RestClient downloadClient) {
        this.pdfDownloadClient = downloadClient;
    }

    /**
     * 원격 PDF를 Spring RestClient로 다운로드하여 바이트 배열로 반환한다.
     * - URI를 그대로 사용해(이중 인코딩 방지) GET 요청 수행
     * - 2xx 가 아니면 내부적으로 예외 발생(RestClient의 기본 동작)
     * - 응답 바이트 검증: 비어있으면 PDF_EMPTY_RESPONSE, 최대 크기 초과면 PDF_TOO_LARGE
     * - 그 외 네트워크/타임아웃/HTTP 예외는 PDF_DOWNLOAD_ERROR로 래핑
     *
     * @param url 다운로드할 PDF의 절대 URL(프리사인드/퍼센트 인코딩 포함 가능)
     * @return 다운로드한 PDF 바이트 배열
     * @throws OcrException 다음의 에러타입으로 발생
     *         - PDF_EMPTY_RESPONSE : 본문이 비어있음
     *         - PDF_TOO_LARGE      : 허용 최대 크기 초과
     *         - PDF_DOWNLOAD_ERROR : 네트워크/HTTP/기타 예외 전반
     */
    public byte[] downloadPdfFromUrl(String url) {
        try {
            ResponseEntity<byte[]> entity = pdfDownloadClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .toEntity(byte[].class);

            byte[] data = entity.getBody();
            if (data == null || data.length == 0) {
                throw new OcrException(OcrErrorType.PDF_EMPTY_RESPONSE);
            }
            if (data.length > MAX_PDF_BYTES) {
                throw new OcrException(OcrErrorType.PDF_TOO_LARGE);
            }
            return data;
        } catch (OcrException e)  {
            throw e; // 이미 처리된 OcrException은 재던짐
        } catch (Exception e) {
            log.error("PDF 다운로드 실패: {}", e.getMessage());
            throw new OcrException(OcrErrorType.PDF_DOWNLOAD_ERROR);
        }
    }
}
