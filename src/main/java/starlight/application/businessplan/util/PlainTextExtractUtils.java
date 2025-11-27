package starlight.application.businessplan.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Content(JSON) → 줄글 변환 (고정 포맷)
 * - text : value
 * - image : "[사진] {caption}" (캡션 없으면 "[사진]")
 * - table : "col1: v1, col2: v2, ..." (행마다 한 줄)
 *
 * 지원 입력:
 * (1) {"content":[ ... ]}
 * (2) {"blocks":[ {"content":[ ... ]}, ... ]}
 */
public final class PlainTextExtractUtils {

    private PlainTextExtractUtils() {
    }

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
                    List<String> tableLines = extractTable_new(contentItem);
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

    static List<String> extractTable_new(JsonNode contentItem) {
        List<String> tableLines = new ArrayList<>();
        JsonNode columnArrayNode = contentItem.path("columns");
        JsonNode rowArrayNode = contentItem.path("rows");
        if (!columnArrayNode.isArray() || !rowArrayNode.isArray()) {
            return tableLines;
        }

        int columnCount = columnArrayNode.size();
        int rowCount = rowArrayNode.size();

        if (rowCount == 0) {
            return tableLines;
        }

        // 컬럼 개수 표시
        tableLines.add("[" + columnCount + " columns]");

        // rowspan과 colspan을 고려하여 실제 테이블 그리드 구성
        // grid[row][col] = 해당 위치의 셀 텍스트 (null이면 rowspan으로 차지된 위치)
        String[][] grid = new String[rowCount][columnCount];
        boolean[][] isOccupied = new boolean[rowCount][columnCount];

        // 각 행을 순회하며 셀 배치
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            JsonNode rowNode = rowArrayNode.get(rowIndex);
            if (!rowNode.isArray()) {
                continue;
            }

            int colIndex = 0;
            for (JsonNode cellNode : rowNode) {
                // rowspan으로 차지된 위치는 건너뛰기
                while (colIndex < columnCount && isOccupied[rowIndex][colIndex]) {
                    colIndex++;
                }

                if (colIndex >= columnCount) {
                    break;
                }

                // 셀 내용 추출
                String cellText = extractCellContent(cellNode);

                // rowspan과 colspan 값 가져오기 (기본값 1)
                int rowspan = cellNode.has("rowspan") ? cellNode.path("rowspan").asInt(1) : 1;
                int colspan = cellNode.has("colspan") ? cellNode.path("colspan").asInt(1) : 1;

                // colspan 범위 확인
                if (colIndex + colspan > columnCount) {
                    colspan = columnCount - colIndex;
                }

                // 셀을 그리드에 배치
                for (int r = 0; r < rowspan && rowIndex + r < rowCount; r++) {
                    for (int c = 0; c < colspan && colIndex + c < columnCount; c++) {
                        if (r == 0 && c == 0) {
                            // 첫 번째 위치에만 텍스트 저장
                            grid[rowIndex + r][colIndex + c] = cellText;
                        }
                        // 모든 위치를 차지된 것으로 표시
                        isOccupied[rowIndex + r][colIndex + c] = true;
                    }
                }

                colIndex += colspan;
            }
        }

        // 그리드를 순회하며 각 행의 텍스트 생성
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            List<String> rowValues = new ArrayList<>();
            for (int colIndex = 0; colIndex < columnCount; colIndex++) {
                String cellText = grid[rowIndex][colIndex];
                if (cellText == null) {
                    // rowspan으로 차지된 위치는 빈 문자열
                    cellText = "";
                }
                rowValues.add("\"" + cellText + "\"");
            }
            tableLines.add("[" + String.join(", ", rowValues) + "]");
        }

        return tableLines;
    }

    /** 셀 내부의 content (BasicContent 리스트)를 텍스트로 추출 */
    static String extractCellContent(JsonNode cellNode) {
        if (!cellNode.has("content") || !cellNode.path("content").isArray()) {
            return "";
        }

        List<String> cellParts = new ArrayList<>();
        for (JsonNode contentItem : cellNode.path("content")) {
            String type = contentItem.path("type").asText("");
            switch (type) {
                case "text":
                    String textValue = contentItem.path("value").asText("");
                    if (!textValue.isBlank()) {
                        cellParts.add(textValue);
                    }
                    break;
                case "image":
                    Optional<String> imageText = extractImage(contentItem);
                    imageText.ifPresent(cellParts::add);
                    break;
                default:
                    break;
            }
        }

        return String.join(" ", cellParts);
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
