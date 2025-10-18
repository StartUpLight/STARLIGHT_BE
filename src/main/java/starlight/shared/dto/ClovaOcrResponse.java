package starlight.shared.dto;

import java.util.List;

public record ClovaOcrResponse(
        String version,
        String requestId,
        long timestamp,
        List<ImageResult> images
) {
    public record ImageResult(
            String uid,
            String name,
            String inferResult,
            String message,
            List<Field> fields
    ) {
        public record Field(
                String valueType,
                String inferText,
                Double inferConfidence,
                String type,
                Boolean lineBreak
        ) {}
    }

    public static ClovaOcrResponse create(String version, String requestId, List<ImageResult> images) {
        return new ClovaOcrResponse(
                version,
                requestId,
                System.currentTimeMillis(),
                images == null ? List.of() : List.copyOf(images)
        );
    }

    public static ClovaOcrResponse createEmpty() {
        return create("V2", "empty", List.of());
    }
}
