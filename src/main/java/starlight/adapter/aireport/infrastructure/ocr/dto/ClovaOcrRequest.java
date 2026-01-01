package starlight.adapter.aireport.infrastructure.ocr.dto;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

public record ClovaOcrRequest(
        String version,
        String requestId,
        long timestamp,
        String lang,
        List<Image> images
) {
    public static ClovaOcrRequest create(String version, String lang, List<Image> images) {
        return new ClovaOcrRequest(
                version,
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                lang,
                images
        );
    }

    public static ClovaOcrRequest createPdfByBytes(String version, byte[] pdfBytes) {
        String b64 = Base64.getEncoder().encodeToString(pdfBytes);
        return create(version, "ko", List.of(Image.ofData("pdf", "input", b64)));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Image(
            String format,
            String name,
            String url,
            String data
    ) {
        public static Image ofData(String format, String name, String base64) {
            return new Image(format, name, null, base64);
        }
    }
}