package starlight.domain.expertApplication.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.util.Assert;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.PlanStatus;
import starlight.shared.AbstractEntity;

@Getter
@ToString
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(
        name = "mentor_application",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_app_active_one",
                        columnNames = {"business_plan_id", "mentor_id"}
                )
        }
)
public class ExpertApplication extends AbstractEntity {

    @Column(nullable = false)
    private Long businessPlanId;

    @Column(nullable = false)
    private Long mentorId;

    public static ExpertApplication create(Long businessPlanId, Long expertId) {
        Assert.notNull(businessPlanId, "businessPlanId must not be null");
        Assert.notNull(expertId, "mentorId must not be null");

        ExpertApplication expertApplication = new ExpertApplication();
        expertApplication.businessPlanId = businessPlanId;
        expertApplication.mentorId = expertId;

        return expertApplication;
    }
}
