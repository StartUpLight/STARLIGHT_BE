package starlight.domain.businessplan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import starlight.domain.businessplan.value.RawJson;

import java.util.List;

@Slf4j
@Getter
@Entity
@NoArgsConstructor
public class GrowthTactic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "raw_json", columnDefinition = "TEXT", nullable = false)
    )
    private RawJson rawJson;

    @Column(nullable = false)
    private boolean checkFirst;

    @Column(nullable = false)
    private boolean checkSecond;

    @Column(nullable = false)
    private boolean checkThird;

    @Column(nullable = false)
    private boolean checkFourth;

    @Column(nullable = false)
    private boolean checkFifth;

    public static GrowthTactic create(RawJson rawJson) {
        Assert.notNull(rawJson, "rawJson must not be null");

        GrowthTactic growthTactic = new GrowthTactic();
        growthTactic.rawJson = rawJson;

        return growthTactic;
    }

    public static GrowthTactic create(String jsonString) {
        Assert.notNull(jsonString, "rawJsonString은 null일 수 없습니다.");

        GrowthTactic growthTactic = new GrowthTactic();
        growthTactic.rawJson = RawJson.create(jsonString);

        return growthTactic;
    }

    public void updateRawJson(String jsonString) {
        Assert.notNull(jsonString, "rawJsonString은 null일 수 없습니다.");

        this.rawJson = RawJson.create(jsonString);
    }

    public void updateChecks(List<Boolean> checks) {
        Assert.notNull(checks, "checks 리스트는 null일 수 없습니다.");
        Assert.isTrue(checks.size() == 5, "checks 리스트는 길이 5 여야 합니다.");

        applyChecks(checks);
    }

    private void applyChecks(List<Boolean> checks) {
        this.checkFirst = Boolean.TRUE.equals(checks.get(0));
        this.checkSecond = Boolean.TRUE.equals(checks.get(1));
        this.checkThird = Boolean.TRUE.equals(checks.get(2));
        this.checkFourth = Boolean.TRUE.equals(checks.get(3));
        this.checkFifth = Boolean.TRUE.equals(checks.get(4));
    }
}
