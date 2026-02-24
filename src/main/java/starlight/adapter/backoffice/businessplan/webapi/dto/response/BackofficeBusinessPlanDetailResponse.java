package starlight.adapter.backoffice.businessplan.webapi.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import starlight.application.backoffice.businessplan.provided.dto.result.BackofficeBusinessPlanDetailResult;
import starlight.application.backoffice.expertapplication.provided.dto.result.BackofficeBusinessPlanExpertApplicationsResult;
import starlight.domain.businessplan.enumerate.PlanStatus;
import starlight.domain.businessplan.enumerate.SubSectionType;

import java.time.LocalDateTime;
import java.util.List;

public record BackofficeBusinessPlanDetailResponse(
        Long businessPlanId,
        String title,
        PlanStatus planStatus,
        Double score,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime submittedAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt,
        MemberResponse member,
        List<SubSectionDetailResponse> subSectionDetailList,
        List<ExpertApplicationResponse> expertApplications
) {
    public static BackofficeBusinessPlanDetailResponse from(
            BackofficeBusinessPlanDetailResult result,
            BackofficeBusinessPlanExpertApplicationsResult expertApplicationsResult
    ) {
        return new BackofficeBusinessPlanDetailResponse(
                result.businessPlanId(),
                result.title(),
                result.planStatus(),
                result.score(),
                result.submittedAt(),
                result.updatedAt(),
                MemberResponse.from(result.member()),
                result.subSectionDetailList().stream().map(SubSectionDetailResponse::from).toList(),
                expertApplicationsResult.expertApplications().stream().map(ExpertApplicationResponse::from).toList()
        );
    }

    public record MemberResponse(
            Long memberId,
            String name,
            String email,
            String provider,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime joinedAt,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime lastActiveAt
    ) {
        public static MemberResponse from(BackofficeBusinessPlanDetailResult.MemberResult result) {
            if (result == null) {
                return null;
            }

            return new MemberResponse(
                    result.memberId(),
                    result.name(),
                    result.email(),
                    result.provider(),
                    result.joinedAt(),
                    result.lastActiveAt()
            );
        }
    }

    public record SubSectionDetailResponse(
            SubSectionType subSectionType,
            Long subSectionId,
            Integer displayOrder,
            String displayNumber,
            String displayName,
            String displayTitle,
            List<ChecklistResponse> checklist,
            JsonNode content
    ) {
        public static SubSectionDetailResponse from(BackofficeBusinessPlanDetailResult.SubSectionDetailResult result) {
            return new SubSectionDetailResponse(
                    result.subSectionType(),
                    result.subSectionId(),
                    result.displayOrder(),
                    result.displayNumber(),
                    result.displayName(),
                    result.displayTitle(),
                    result.checklist().stream().map(ChecklistResponse::from).toList(),
                    result.content()
            );
        }
    }

    public record ChecklistResponse(
            String title,
            String content,
            boolean checked
    ) {
        public static ChecklistResponse from(BackofficeBusinessPlanDetailResult.ChecklistResult result) {
            return new ChecklistResponse(result.title(), result.content(), result.checked());
        }
    }

    public record ExpertApplicationResponse(
            Long applicationId,
            Long expertId,
            String expertName,
            String status,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime requestedAt,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime feedbackUpdatedAt,
            Integer feedbackScore,
            String feedbackSummary,
            List<String> strengths,
            List<String> weaknesses
    ) {
        public static ExpertApplicationResponse from(
                BackofficeBusinessPlanExpertApplicationsResult.ExpertApplicationResult result
        ) {
            return new ExpertApplicationResponse(
                    result.applicationId(),
                    result.expertId(),
                    result.expertName(),
                    result.status(),
                    result.requestedAt(),
                    result.feedbackUpdatedAt(),
                    result.feedbackScore(),
                    result.feedbackSummary(),
                    result.strengths(),
                    result.weaknesses()
            );
        }
    }
}
