package starlight.domain.aireport.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;
import starlight.domain.businessplan.value.RawJson;
import starlight.shared.domain.AbstractEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiReport extends AbstractEntity {

    @Column(name = "business_plan_id", nullable = false)
    private Long businessPlanId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "raw_json", columnDefinition = "TEXT", nullable = false))
    private RawJson rawJson;

    public static AiReport create(Long businessPlanId, String rawJson) {
        Assert.notNull(businessPlanId, "businessPlanId must not be null");
        Assert.notNull(rawJson, "rawJson은 null일 수 없습니다.");

        AiReport report = new AiReport();
        report.businessPlanId = businessPlanId;
        report.rawJson = RawJson.create(rawJson);

        return report;
    }

    public void update(String rawJson) {
        Assert.notNull(rawJson, "rawJson은 null일 수 없습니다.");

        this.rawJson = RawJson.create(rawJson);
    }
}
