package starlight.application.businessplan.strategy.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Content(JSON) → 줄글 변환 (고정 포맷)
 *  - text  : value
 *  - image : "[사진] {caption}" (캡션 없으면 "[사진]")
 *  - table : "col1: v1, col2: v2, ..." (행마다 한 줄)
 *
 * 지원 입력:
 *  (1) {"content":[ ... ]}
 *  (2) {"blocks":[ {"content":[ ... ]}, ... ]}
 */
public final class ContentPlainText {

    private ContentPlainText() {}

    // 고정 포맷 상수
    private static final String IMAGE_TOKEN = "[사진]";
    private static final String TABLE_PAIR_SEPARATOR = ": ";
    private static final String TABLE_FIELD_SEPARATOR = ", ";

    /** raw JSON 문자열 → 줄글 */
    public static String extractPlainText(ObjectMapper objectMapper, String jsonString) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            return extractPlainText(rootNode);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON", e);
        }
    }

    /** DTO → 줄글 */
    public static String extractPlainText(ObjectMapper objectMapper, Object dtoObject) {
        try {
            JsonNode rootNode = objectMapper.valueToTree(dtoObject);
            return extractPlainText(rootNode);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot serialize DTO", e);
        }
    }

    /** 핵심 처리 */
    static String extractPlainText(JsonNode rootNode) {
        List<String> outputLines = new ArrayList<>();
        List<JsonNode> contentItems = findContentItems(rootNode);

        for (JsonNode contentItem : contentItems) {
            String type = contentItem.path("type").asText("");

            switch (type) {
                case "text":
                    Optional<String> textLine = extractText(contentItem);
                    textLine.ifPresent(outputLines::add);
                    break;

                case "image":
                    Optional<String> imageLine = extractImage(contentItem);
                    imageLine.ifPresent(outputLines::add);
                    break;

                case "table":
                    List<String> tableLines = extractTable(contentItem);
                    outputLines.addAll(tableLines);
                    break;

                default:
                    break;
            }
        }
        return joinNonBlank(outputLines, "\n").trim();
    }

    /** content 아이템 전부 추출(순서 보존) */
    static List<JsonNode> findContentItems(JsonNode rootNode) {
        List<JsonNode> contentItems = new ArrayList<>();

        // case (1): content 바로 있는 경우
        if (rootNode.has("content") && rootNode.path("content").isArray()) {
            addAll(contentItems, (ArrayNode) rootNode.path("content"));
            return contentItems;
        }

        // case (2): blocks 내부 content들
        if (rootNode.has("blocks") && rootNode.path("blocks").isArray()) {
            for (JsonNode blockNode : rootNode.path("blocks")) {
                if (blockNode.has("content") && blockNode.path("content").isArray()) {
                    addAll(contentItems, (ArrayNode) blockNode.path("content"));
                }
            }
        }
        return contentItems;
    }

    /** 텍스트 항목 */
    static Optional<String> extractText(JsonNode contentItem) {
        String value = contentItem.path("value").asText("");
        return value.isBlank() ? Optional.empty() : Optional.of(value);
    }

    /** 이미지 항목: "[사진]" + (있으면 공백+캡션) */
    static Optional<String> extractImage(JsonNode contentItem) {
        String caption = contentItem.hasNonNull("caption") ? contentItem.path("caption").asText().trim() : "";
        if (caption.isBlank()) {
            return Optional.of(IMAGE_TOKEN);
        }
        return Optional.of(IMAGE_TOKEN + " " + caption);
    }

    /** 테이블 항목: 각 행을 "col1: v1, col2: v2 ..." */
    static List<String> extractTable(JsonNode contentItem) {
        List<String> tableLines = new ArrayList<>();
        JsonNode columnArrayNode = contentItem.path("columns");
        JsonNode rowArrayNode = contentItem.path("rows");
        if (!columnArrayNode.isArray() || !rowArrayNode.isArray()) {
            return tableLines;
        }

        for (JsonNode rowNode : rowArrayNode) {
            String line = mapRow(columnArrayNode, rowNode);
            if (!line.isBlank()) {
                tableLines.add(line);
            }
        }
        return tableLines;
    }

    /** 한 행을 "col: val, col: val" 형태로 매핑 */
    static String mapRow(JsonNode columnArrayNode, JsonNode rowArrayNode) {
        StringBuilder rowBuilder = new StringBuilder();
        int columnIndex = 0;
        Iterator<JsonNode> columnIterator = columnArrayNode.elements();

        while (columnIterator.hasNext()) {
            String columnName = columnIterator.next().asText("");
            String cellValue = rowArrayNode.has(columnIndex) ? rowArrayNode.get(columnIndex).asText("") : "";
            if (!columnName.isBlank()) {
                if (rowBuilder.length() > 0) {
                    rowBuilder.append(TABLE_FIELD_SEPARATOR);
                }
                rowBuilder.append(columnName).append(TABLE_PAIR_SEPARATOR).append(cellValue);
            }
            columnIndex++;
        }
        return rowBuilder.toString();
    }

    /** ArrayNode → List에 추가 */
    static void addAll(List<JsonNode> targetList, ArrayNode sourceArrayNode) {
        for (JsonNode node : sourceArrayNode) {
            targetList.add(node);
        }
    }

    /** 공백/빈 문자열 제외 후 결합 */
    static String joinNonBlank(List<String> partsList, String separator) {
        String result = "";
        for (String part : partsList) {
            if (part != null && !part.isBlank()) {
                result = result.isEmpty() ? part : result + separator + part;
            }
        }
        return result;
    }
}
