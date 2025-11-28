package starlight.adapter.businessplan.webapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.List;

public record SubSectionCreateRequest(
                @NotNull SubSectionType subSectionType,
                @NotNull List<Boolean> checks,
                @Valid @NotNull Meta meta,
                @Valid @NotNull List<@Valid Block> blocks) {
        public record Meta(
                        @NotBlank String author,
                        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$") String createdAt) {
        }

        public record Block(
                        @Valid @NotNull BlockMeta meta,
                        @Valid List<@Valid GeneralContent> content) {
        }

        public record BlockMeta(
                        @NotBlank String title) {
        }

        // 테이블 셀 내부에서 사용하는 기본 콘텐츠 (TextItem, ImageItem만)
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
        @JsonSubTypes({
                        @JsonSubTypes.Type(value = TextItem.class, name = "text"),
                        @JsonSubTypes.Type(value = ImageItem.class, name = "image")
        })
        public sealed interface BasicContent
                        permits TextItem, ImageItem {
                String type();
        }

        // 블록의 content에서 사용하는 일반 콘텐츠 (BasicContent + TableItem)
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
        @JsonSubTypes({
                        @JsonSubTypes.Type(value = TextItem.class, name = "text"),
                        @JsonSubTypes.Type(value = ImageItem.class, name = "image"),
                        @JsonSubTypes.Type(value = TableItem.class, name = "table")
        })
        public sealed interface GeneralContent
                        permits TextItem, ImageItem, TableItem {
                String type();
        }

        public record TextItem(
                        @NotBlank String type,
                        String value) implements BasicContent, GeneralContent {
        }

        public record ImageItem(
                        @NotBlank String type,
                        @NotBlank @Size(max = 1024) String src,
                        @JsonProperty(defaultValue = "400") Integer width,
                        @JsonProperty(defaultValue = "400") Integer height,
                        @Size(max = 255) String caption) implements BasicContent, GeneralContent {
                public ImageItem {
                        width = width != null ? width : 400;
                        height = height != null ? height : 400;
                }
        }

        // 컬럼 정보 (헤더 제거, 개수와 너비만)
        public record TableColumn(
                        Integer width) {
        }

        // 셀 데이터
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public record TableCell(
                        @NotEmpty List<@Valid BasicContent> content,
                        @Min(1) Integer rowspan,
                        @Min(1) Integer colspan) {
                public TableCell {
                        rowspan = rowspan != null ? rowspan : 1;
                        colspan = colspan != null ? colspan : 1;
                }
        }

        public record TableItem(
                        @NotBlank String type,
                        @NotEmpty List<@Valid TableColumn> columns,
                        @NotEmpty List<List<@Valid TableCell>> rows) implements GeneralContent {

                @AssertTrue(message = "table rows must match columns length considering cell spans")
                @JsonIgnore
                public boolean isValidCellSpans() {
                        if (columns == null || rows == null || rows.isEmpty())
                                return false;

                        int expectedWidth = columns.size();
                        int rowCount = rows.size();

                        // 각 위치에서 rowspan으로 차지된 셀을 추적
                        // grid[row][col] = 위에서 내려온 rowspan 셀의 남은 rowspan 값 (0이면 비어있음)
                        int[][] grid = new int[rowCount][expectedWidth];

                        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                                var row = rows.get(rowIndex);
                                if (row == null)
                                        return false;

                                // 현재 행에서 rowspan으로 차지된 위치 수 계산
                                int occupiedByRowspan = 0;
                                for (int col = 0; col < expectedWidth; col++) {
                                        if (grid[rowIndex][col] > 0) {
                                                occupiedByRowspan++;
                                        }
                                }

                                // 빈 행인 경우 (모든 위치가 rowspan으로 차지됨)
                                if (row.isEmpty()) {
                                        if (occupiedByRowspan == expectedWidth) {
                                                // 빈 행이 유효함 (모든 위치가 rowspan으로 차지됨)
                                                continue;
                                        } else {
                                                // 빈 행인데 rowspan으로 차지되지 않은 위치가 있음
                                                return false;
                                        }
                                }

                                // 현재 행의 셀들의 colspan 합 계산
                                int rowCellWidth = 0;
                                int colIndex = 0;

                                for (var cell : row) {
                                        if (cell == null)
                                                return false;

                                        // rowspan으로 차지된 위치는 건너뛰기 (HTML 테이블 규칙)
                                        while (colIndex < expectedWidth && grid[rowIndex][colIndex] > 0) {
                                                colIndex++;
                                        }

                                        if (colIndex >= expectedWidth) {
                                                // 컬럼 범위를 벗어남
                                                return false;
                                        }

                                        // colspan 범위 확인
                                        if (colIndex + cell.colspan() > expectedWidth) {
                                                return false;
                                        }

                                        // colspan 범위 내에 rowspan으로 차지된 셀이 있는지 확인 (HTML 테이블 규칙)
                                        for (int i = 0; i < cell.colspan(); i++) {
                                                if (grid[rowIndex][colIndex + i] > 0) {
                                                        // rowspan으로 이미 차지된 위치와 겹침
                                                        return false;
                                                }
                                        }

                                        // 셀을 그리드에 배치 (rowspan이 있으면 아래 행들도 표시)
                                        int cellRowspan = cell.rowspan();
                                        int cellColspan = cell.colspan();

                                        for (int r = 0; r < cellRowspan && rowIndex + r < rowCount; r++) {
                                                for (int c = 0; c < cellColspan; c++) {
                                                        if (rowIndex + r < rowCount && colIndex + c < expectedWidth) {
                                                                grid[rowIndex + r][colIndex + c] = cellRowspan - r;
                                                        }
                                                }
                                        }

                                        rowCellWidth += cellColspan;
                                        colIndex += cellColspan;
                                }

                                // 각 행의 실제 너비는 (컬럼 수 - rowspan으로 차지된 위치 수)와 일치해야 함
                                if (rowCellWidth != expectedWidth - occupiedByRowspan) {
                                        return false;
                                }
                        }

                        return true;
                }
        }
}
