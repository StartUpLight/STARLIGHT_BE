package starlight.adapter.ncp.ocr.infra;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import starlight.adapter.ncp.ocr.dto.ClovaOcrRequest;
import starlight.adapter.ncp.ocr.exception.OcrErrorType;
import starlight.adapter.ncp.ocr.exception.OcrException;
import starlight.shared.dto.ClovaOcrResponse;

@Slf4j
@Component
public class ClovaOcrClient {

    private final RestClient clovaOcrRestClient;

    public ClovaOcrClient(@Qualifier("clovaOcrRestClient") RestClient restClient) {
        this.clovaOcrRestClient = restClient;
    }

    public ClovaOcrResponse recognizePdfBytes(byte[] pdfBytes) {
        ClovaOcrRequest request = ClovaOcrRequest.createPdfByBytes("V2", pdfBytes);

        try {
            return clovaOcrRestClient.post()
                    .body(request)
                    .retrieve()
                    .body(ClovaOcrResponse.class);
        } catch (Exception e) {
            log.warn("CLOVA OCR 호출 실패: {}", e.getMessage());
            throw new OcrException(OcrErrorType.OCR_CLIENT_ERROR);
        }
    }
}
