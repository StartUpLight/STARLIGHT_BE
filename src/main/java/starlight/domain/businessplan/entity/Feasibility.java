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
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "feasibility_strategy_id", unique = true)
    private SubSection feasibilityStrategy;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "feasibility_market_id", unique = true)
    private SubSection feasibilityMarket;

    public static Feasibility create() {
        Feasibility feasibility = new Feasibility();
        feasibility.initializeSubSections();
        return feasibility;
    }

    @SuppressWarnings("deprecation")
    private void initializeSubSections() {
        this.feasibilityStrategy = SubSection.createEmptySubSection(SubSectionName.FEASIBILITY_STRATEGY);
        this.feasibilityStrategy.attachToFeasibility(this);

        this.feasibilityMarket = SubSection.createEmptySubSection(SubSectionName.FEASIBILITY_MARKET);
        this.feasibilityMarket.attachToFeasibility(this);
    }

    public void setSubSectionByType(SubSection subSection) {
        switch (subSection.getSubSectionName()) {
            case FEASIBILITY_STRATEGY -> this.feasibilityStrategy = subSection;
            case FEASIBILITY_MARKET -> this.feasibilityMarket = subSection;
        }
    }
}
