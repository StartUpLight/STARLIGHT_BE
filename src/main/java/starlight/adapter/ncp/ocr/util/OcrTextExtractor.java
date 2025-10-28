package starlight.adapter.ncp.ocr.util;

import starlight.shared.dto.infrastructure.ClovaOcrResponse;

import java.util.ArrayList;
import java.util.List;

public final class OcrTextExtractor {

    private OcrTextExtractor() {}

    private static final double MINIMUM_CONFIDENCE_THRESHOLD = 0.85;

    /**
     * 모든 페이지를 하나의 문자열로 병합.
     * 페이지 사이에는 "\n\n-----\n\n" 구분선을 넣는다.
     *
     * @param response                  OCR 원본 응답 (널 가능)
     * @return                          결합된 전체 텍스트 (널 입력이면 빈 문자열)
     */
    public static String toPlainText(ClovaOcrResponse response) {
        List<String> pageTexts = toPages(response);
        return String.join("\n\n-----\n\n", pageTexts);
    }

    /**
     * 페이지(= images 배열의 각 요소)별 텍스트를 리스트로 반환.
     *
     * @param clovaOcrResponse          OCR 원본 응답
     * @return                          각 페이지의 문자열 (images가 비었거나 널이면 빈 리스트)
     */
    public static List<String> toPages(ClovaOcrResponse clovaOcrResponse) {
        List<String> pages = new ArrayList<>();
        if (clovaOcrResponse == null || clovaOcrResponse.images() == null) {
            return pages;
        }

        for (ClovaOcrResponse.ImageResult page : clovaOcrResponse.images()) {
            if (page == null || page.fields() == null || page.fields().isEmpty()) {
                pages.add("");
                continue;
            }

            StringBuilder pageBuilder = new StringBuilder();
            boolean atLineStart = true;

            for (ClovaOcrResponse.ImageResult.Field fieldItem : page.fields()) {
                if (fieldItem == null) {
                    continue;
                }

                Double confidence = fieldItem.inferConfidence();
                if (confidence == null || confidence < MINIMUM_CONFIDENCE_THRESHOLD) {
                    continue;
                }

                String normalizedToken = normalize(fieldItem.inferText());
                if (normalizedToken.isEmpty()) {
                    continue;
                }

                if (!atLineStart) {
                    pageBuilder.append(' ');
                }

                pageBuilder.append(normalizedToken);
                if (Boolean.TRUE.equals(fieldItem.lineBreak())) {
                    pageBuilder.append('\n');
                    atLineStart = true;
                } else {
                    atLineStart = false;
                }
            }

            pages.add(pageBuilder.toString().strip());
        }

        return pages;
    }

    /**
     * 토큰 정규화:
     * - null → "" (스킵되도록)
     * - 앞뒤 공백 제거
     * - 연속 공백 1칸으로 축약
     * - 구두점 앞의 공백 제거, 괄호 주변 공백 정리
     */
    private static String normalize(String raw) {
        if (raw == null) return "";
        String out = raw.strip()
                .replaceAll("\\s+", " ");
        out = out.replaceAll("\\s+([,.:;!?])", "$1")
                .replaceAll("\\(\\s+", "(")
                .replaceAll("\\s+\\)", ")");
        return out;
    }
}
