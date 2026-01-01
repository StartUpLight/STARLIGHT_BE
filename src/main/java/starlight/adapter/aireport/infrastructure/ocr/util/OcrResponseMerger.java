package starlight.adapter.aireport.infrastructure.ocr.util;

import starlight.shared.dto.infrastructure.OcrResponse;

import java.util.ArrayList;
import java.util.List;

public final class OcrResponseMerger {

    private OcrResponseMerger() {}

    public static OcrResponse merge(List<OcrResponse> parts) {
        if (parts == null || parts.isEmpty()) {
            return OcrResponse.createEmpty();
        }

        OcrResponse first = parts.get(0);

        List<OcrResponse.ImageResult> images = new ArrayList<>();
        for (OcrResponse resp : parts) {
            if (resp.images() != null) {
                images.addAll(resp.images());
            }
        }

        return OcrResponse.create(first.version(), first.requestId(), images);
    }
}
