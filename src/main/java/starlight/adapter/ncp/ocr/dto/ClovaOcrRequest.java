package starlight.adapter.ncp.ocr.dto;


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

    /** URL로 PDF 1개 보낼 때 */
    public static ClovaOcrRequest createPdfByUrl(String version, String lang, String pdfUrl) {
        return create(version, lang, List.of(Image.ofUrl("pdf", "input", pdfUrl)));
    }

    /** 바이트(pdf)를 base64로 인코딩해서 1개 이미지 항목으로 보냄 */
    public static ClovaOcrRequest createPdfByBytes(String version, byte[] pdfBytes) {
        String b64 = Base64.getEncoder().encodeToString(pdfBytes);
        return create(version, "ko", List.of(Image.ofData("pdf", "input", b64)));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Image(
            String format, // "pdf"
            String name,   // "input"
            String url,    // url 또는
            String data    // base64 (둘 중 하나만)
    ) {
        public static Image ofUrl(String format, String name, String url) {
            return new Image(format, name, url, null);
        }
        public static Image ofData(String format, String name, String base64) {
            return new Image(format, name, null, base64);
        }
    }
}