package starlight.application.businessplan.strategy.dto;

import com.fasterxml.jackson.databind.JsonNode;
import starlight.domain.businessplan.enumerate.SectionName;

public sealed interface SectionResponse {

    record Created(
            SectionName section,
            Long sectionId,
            String message
    ) implements SectionResponse {}

    record Retrieved(
            String message,
            JsonNode content
    ) implements SectionResponse {}

    record Updated(
            SectionName section,
            Long sectionId,
            String message
    ) implements SectionResponse {}

    record Deleted(
            SectionName section,
            Long sectionId,
            String message
    ) implements SectionResponse {}
}