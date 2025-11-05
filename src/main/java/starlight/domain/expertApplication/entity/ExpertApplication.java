package starlight.domain.expertApplication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.util.Assert;
import starlight.shared.AbstractEntity;

@Getter
@ToString
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
//@Table(
//        uniqueConstraints = {
//                @UniqueConstraint(
//                        name = "uk_app_active_one",
//                        columnNames = {"business_plan_id", "expert_id"}
//                )
//        }
//)
public class ExpertApplication extends AbstractEntity {

    @Column(nullable = false)
    private Long businessPlanId;

    @Column(nullable = false)
    private Long expertId;

    public static ExpertApplication create(Long businessPlanId, Long expertId) {
        Assert.notNull(businessPlanId, "businessPlanId must not be null");
        Assert.notNull(expertId, "expertId must not be null");

        ExpertApplication expertApplication = new ExpertApplication();
        expertApplication.businessPlanId = businessPlanId;
        expertApplication.expertId = expertId;

        return expertApplication;
    }
}
