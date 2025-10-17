package starlight.adapter.ncp.ocr.util;

import starlight.shared.dto.ClovaOcrResponse;

import java.util.ArrayList;
import java.util.List;

public final class OcrResponseMerger {

    private OcrResponseMerger() {}

    public static ClovaOcrResponse merge(List<ClovaOcrResponse> parts) {
        if (parts == null || parts.isEmpty()) {
            return ClovaOcrResponse.createEmpty();
        }

        ClovaOcrResponse first = parts.get(0);

        List<ClovaOcrResponse.ImageResult> images = new ArrayList<>();
        for (ClovaOcrResponse resp : parts) {
            if (resp.images() != null) {
                images.addAll(resp.images());
            }
        }

        return ClovaOcrResponse.create(first.version(), first.requestId(), images);
    }
}
