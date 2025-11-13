package starlight.application.businessplan.provided.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.PlanStatus;

import java.time.LocalDateTime;
import java.util.List;

public record BusinessPlanResponse() {

    public record Result(
            Long businessPlanId,
            String title,
            PlanStatus planStatus,
            String message
    ) {
        public static Result from(BusinessPlan businessPlan, String message) {
            return new Result(
                    businessPlan.getId(),
                    businessPlan.getTitle(),
                    businessPlan.getPlanStatus(),
                    message
            );
        }
    }

    public record Detail(
            Long businessPlanId,
            String title,
            PlanStatus planStatus,
            List<SubSectionResponse.Detail> subSectionDetailList
    ) {
        public static Detail from(
                BusinessPlan businessPlan,
                List<SubSectionResponse.Detail> subSectionDetailList
        ) {
            return new Detail(
                    businessPlan.getId(),
                    businessPlan.getTitle(),
                    businessPlan.getPlanStatus(),
                    subSectionDetailList
            );
        }
    }

    public record Preview(
            Long businessPlanId,
            String title,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime lastSavedAt,
            PlanStatus planStatus
    ) {
        public static Preview from(BusinessPlan businessPlan) {
            LocalDateTime lastSavedAt = businessPlan.getModifiedAt() != null
                    ? businessPlan.getModifiedAt()
                    : businessPlan.getCreatedAt();

            return new Preview(
                    businessPlan.getId(),
                    businessPlan.getTitle(),
                    lastSavedAt,
                    businessPlan.getPlanStatus()
            );
        }

        public static List<Preview> fromAll(List<BusinessPlan> businessPlans) {
            return businessPlans.stream()
                    .map(Preview::from)
                    .toList();
        }
    }
}
