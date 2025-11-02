package starlight.adapter.ncp.ocr.infra;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import starlight.adapter.ncp.ocr.dto.ClovaOcrRequest;
import starlight.adapter.ncp.ocr.exception.OcrErrorType;
import starlight.adapter.ncp.ocr.exception.OcrException;
import starlight.shared.dto.infrastructure.OcrResponse;

@Slf4j
@Component
public class ClovaOcrClient {

    private final RestClient clovaOcrRestClient;

    public ClovaOcrClient(@Qualifier("clovaOcrRestClient") RestClient restClient) {
        this.clovaOcrRestClient = restClient;
    }

    public OcrResponse recognizePdfBytes(byte[] pdfBytes) {
        ClovaOcrRequest request = ClovaOcrRequest.createPdfByBytes("V2", pdfBytes);

        try {
            OcrResponse resp = clovaOcrRestClient.post()
                    .body(request)
                    .retrieve()
                    .body(OcrResponse.class);

            if (resp == null) {
                log.warn("CLOVA OCR 응답이 null 입니다.");
            }

            return resp;
        } catch (Exception e) {
            log.warn("CLOVA OCR 호출 실패", e);
            throw new OcrException(OcrErrorType.OCR_CLIENT_ERROR);
        }
    }
}
