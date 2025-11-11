package starlight.adapter.businessplan.webapi.dto;

import com.fasterxml.jackson.databind.JsonNode;
import starlight.application.businessplan.dto.SubSectionResponse;
import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.Collection;
import java.util.List;

public record BusinessPlanSubSectionResponse(

        SubSectionType subSectionType,

        Long subSectionId,

        JsonNode content
) {

    public static BusinessPlanSubSectionResponse from(SubSectionResponse.Snapshot snapshot) {
        return new BusinessPlanSubSectionResponse(
                snapshot.subSectionType(),
                snapshot.subSectionId(),
                snapshot.content()
        );
    }

    public static List<BusinessPlanSubSectionResponse> fromAll(Collection<SubSectionResponse.Snapshot> snapshots) {
        return snapshots.stream()
                .map(BusinessPlanSubSectionResponse::from)
                .toList();
    }
}

