package starlight.adapter.ncp.ocr.infra;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import starlight.adapter.ncp.ocr.dto.ClovaOcrRequest;
import starlight.shared.dto.ClovaOcrResponse;
import starlight.adapter.ncp.ocr.exception.OcrErrorType;
import starlight.adapter.ncp.ocr.exception.OcrException;

@Slf4j
@Component
public class ClovaOcrClient {

    private final RestClient clovaOcrRestClient;

    public ClovaOcrClient(@Qualifier("clovaOcrRestClient") RestClient restClient) {
        this.clovaOcrRestClient = restClient;
    }

    // 제네릭 없이 OCR 전용 리트라이
    public ClovaOcrResponse recognizePdfBytes(byte[] pdfBytes) {
        ClovaOcrRequest clovaOcrRequest = ClovaOcrRequest.createPdfByBytes("V2", pdfBytes);
        try {
            return postWithRetryForOcr(clovaOcrRequest);
        } catch (ResourceAccessException e) {
            log.info("CLOVA OCR 타임아웃/네트워크 실패: {}", e.getMessage());
            throw new OcrException(OcrErrorType.OCR_TIMEOUT);
        } catch (RestClientResponseException e) {
            log.info("CLOVA OCR 호출 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new OcrException(OcrErrorType.OCR_CLIENT_ERROR);
        }
    }

    private ClovaOcrResponse postWithRetryForOcr(Object body) {
        final int maxAttempts = 3;
        long backoffMs = 800L; // 0.8s → 1.6s → 3.2s

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return clovaOcrRestClient.post()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(ClovaOcrResponse.class);

            } catch (ResourceAccessException e) {
                if (attempt == maxAttempts) {
                    throw e; // 타임아웃/네트워크 오류 최종 실패
                }
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                backoffMs *= 2L;
            }
        }
        throw new IllegalStateException("Unreachable");
    }
}
