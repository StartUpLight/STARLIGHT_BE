package starlight.shared.dto.infrastructure;

import java.util.List;

public record OcrResponse(
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

    public static OcrResponse create(String version, String requestId, List<ImageResult> images) {
        return new OcrResponse(
                version,
                requestId,
                System.currentTimeMillis(),
                images == null ? List.of() : List.copyOf(images)
        );
    }

    public static OcrResponse createEmpty() {
        return create("V2", "empty", List.of());
    }
}
