package starlight.domain.businessplan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import starlight.domain.businessplan.enumerate.SubSectionName;

@Slf4j
@Getter
@Entity
@NoArgsConstructor
public class ProblemRecognition {
    @Id
    @Column(name = "business_plan_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "business_plan_id", referencedColumnName = "id")
    private BusinessPlan businessPlan;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_background_id")
    private SubSection problemBackground;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_purpose_id")
    private SubSection problemPurpose;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_market_id")
    private SubSection problemMarket;

    public static ProblemRecognition create() {
        ProblemRecognition problemRecognition = new ProblemRecognition();
        return problemRecognition;
    }

    /**
     * 양방향 매핑을 위한 메서드
     */
    public void setSubSectionByType(SubSection subSection) {
        switch (subSection.getSubSectionName()) {
            case PROBLEM_BACKGROUND -> this.problemBackground = subSection;
            case PROBLEM_PURPOSE -> this.problemPurpose = subSection;
            case PROBLEM_MARKET -> this.problemMarket = subSection;
        }
    }

    public void attachBusinessPlan(BusinessPlan businessPlan) {
        this.businessPlan = businessPlan;
    }
}
