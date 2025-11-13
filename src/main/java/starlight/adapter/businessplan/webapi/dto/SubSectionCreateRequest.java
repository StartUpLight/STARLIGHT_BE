package starlight.adapter.businessplan.webapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.List;

public record SubSectionCreateRequest(
                @NotNull SubSectionType subSectionType,
                @NotNull List<Boolean> checks,
                @Valid @NotNull SubSectionCreateRequest.Meta meta,
                @Valid @NotNull List<@Valid Block> blocks) {
        public record Meta(
                        @NotBlank String author,
                        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$") String createdAt) {
        }

        public record Block(
                        @Valid @NotNull SubSectionCreateRequest.BlockMeta meta,
                        @Valid List<@Valid Content> content) {
        }

        public record BlockMeta(
                        @NotBlank String title) {
        }

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
        @JsonSubTypes({
                        @JsonSubTypes.Type(value = SubSectionCreateRequest.TextItem.class, name = "text"),
                        @JsonSubTypes.Type(value = SubSectionCreateRequest.ImageItem.class, name = "image"),
                        @JsonSubTypes.Type(value = SubSectionCreateRequest.TableItem.class, name = "table")
        })
        public sealed interface Content
                        permits SubSectionCreateRequest.TextItem, SubSectionCreateRequest.ImageItem, SubSectionCreateRequest.TableItem {
                String type();
        }

        public record TextItem(
                        @NotBlank String type,
                        @NotBlank String value) implements SubSectionCreateRequest.Content {
        }

        public record ImageItem(
                        @NotBlank String type,
                        @NotBlank @Size(max = 1024) String src,
                        @Size(max = 255) String caption) implements SubSectionCreateRequest.Content {
        }

        public record TableItem(
                        @NotBlank String type,
                        @NotEmpty List<@NotBlank String> columns,
                        @NotEmpty List<@NotEmpty List<Object>> rows) implements SubSectionCreateRequest.Content {

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
