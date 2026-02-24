package starlight.application.backoffice.businessplan.provided.dto.result;

import com.fasterxml.jackson.databind.JsonNode;
import starlight.domain.businessplan.enumerate.PlanStatus;
import starlight.domain.businessplan.enumerate.SubSectionType;

import java.time.LocalDateTime;
import java.util.List;

public record BackofficeBusinessPlanDetailResult(
        Long businessPlanId,
        String title,
        PlanStatus planStatus,
        Double score,
        LocalDateTime submittedAt,
        LocalDateTime updatedAt,
        MemberResult member,
        List<SubSectionDetailResult> subSectionDetailList
) {
    public static BackofficeBusinessPlanDetailResult of(
            Long businessPlanId,
            String title,
            PlanStatus planStatus,
            Double score,
            LocalDateTime submittedAt,
            LocalDateTime updatedAt,
            MemberResult member,
            List<SubSectionDetailResult> subSectionDetailList
    ) {
        return new BackofficeBusinessPlanDetailResult(
                businessPlanId,
                title,
                planStatus,
                score,
                submittedAt,
                updatedAt,
                member,
                subSectionDetailList
        );
    }

    public record MemberResult(
            Long memberId,
            String name,
            String email,
            String provider,
            LocalDateTime joinedAt,
            LocalDateTime lastActiveAt
    ) {
        public static MemberResult of(
                Long memberId,
                String name,
                String email,
                String provider,
                LocalDateTime joinedAt,
                LocalDateTime lastActiveAt
        ) {
            return new MemberResult(memberId, name, email, provider, joinedAt, lastActiveAt);
        }
    }

    public record SubSectionDetailResult(
            SubSectionType subSectionType,
            Long subSectionId,
            Integer displayOrder,
            String displayNumber,
            String displayName,
            String displayTitle,
            List<ChecklistResult> checklist,
            JsonNode content
    ) {
        public static SubSectionDetailResult of(
                SubSectionType subSectionType,
                Long subSectionId,
                Integer displayOrder,
                String displayNumber,
                String displayName,
                String displayTitle,
                List<ChecklistResult> checklist,
                JsonNode content
        ) {
            return new SubSectionDetailResult(
                    subSectionType,
                    subSectionId,
                    displayOrder,
                    displayNumber,
                    displayName,
                    displayTitle,
                    checklist,
                    content
            );
        }
    }

    public record ChecklistResult(
            String title,
            String content,
            boolean checked
    ) {
        public static ChecklistResult of(String title, String content, boolean checked) {
            return new ChecklistResult(title, content, checked);
        }
    }
}
