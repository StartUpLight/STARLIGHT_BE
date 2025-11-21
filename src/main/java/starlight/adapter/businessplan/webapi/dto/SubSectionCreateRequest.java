package starlight.adapter.businessplan.webapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
                        @Valid List<@Valid Content> content) {
        }

        public record BlockMeta(
                        @NotBlank String title) {
        }

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
        @JsonSubTypes({
                        @JsonSubTypes.Type(value = TextItem.class, name = "text"),
                        @JsonSubTypes.Type(value = ImageItem.class, name = "image"),
                        @JsonSubTypes.Type(value = TableItem.class, name = "table")
        })
        public sealed interface Content
                        permits TextItem, ImageItem, TableItem {
                String type();
        }

        public record TextItem(
                        @NotBlank String type,
                        @NotBlank String value) implements Content {
        }

        public record ImageItem(
                        @NotBlank String type,
                        @NotBlank @Size(max = 1024) String src,
                        @JsonProperty(defaultValue = "400") Integer width,
                        @JsonProperty(defaultValue = "400") Integer height,
                        @Size(max = 255) String caption) implements Content {
                public ImageItem {
                        width = width != null ? width : 400;
                        height = height != null ? height : 400;
                }
        }

        public record TableItem(
                        @NotBlank String type,
                        @NotEmpty List<@NotBlank String> columns,
                        @NotEmpty List<@NotEmpty List<Object>> rows) implements Content {

                @AssertTrue(message = "table rows must match columns length")
                @JsonIgnore
                public boolean isRectangular() {
                        int w = (columns == null) ? -1 : columns.size();
                        if (w <= 0 || rows == null || rows.isEmpty())
                                return false;
                        for (var r : rows)
                                if (r == null || r.size() != w)
                                        return false;
                        return true;
                }
        }
}
