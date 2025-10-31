package starlight.domain.businessplan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import starlight.domain.businessplan.enumerate.SubSectionName;

@Getter
@Entity
@NoArgsConstructor
public class Feasibility {
    @Id
    @Column(name="business_plan_id")
    private Long id;

    @OneToOne @MapsId
    @JoinColumn(name = "business_plan_id", referencedColumnName = "id")
    private BusinessPlan businessPlan;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "feasibility_strategy_id", unique = true)
    private SubSection feasibilityStrategy;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "feasibility_market_id", unique = true)
    private SubSection feasibilityMarket;

    public static Feasibility create() {
        Feasibility feasibility = new Feasibility();
        return feasibility;
    }

    public void setSubSectionByType(SubSection subSection) {
        switch (subSection.getSubSectionName()) {
            case FEASIBILITY_STRATEGY -> this.feasibilityStrategy = subSection;
            case FEASIBILITY_MARKET -> this.feasibilityMarket = subSection;
        }
    }

    public void attachBusinessPlan(BusinessPlan businessPlan) {
        this.businessPlan = businessPlan;
    }
}
