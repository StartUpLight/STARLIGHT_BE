package starlight.domain.businessplan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import starlight.domain.businessplan.enumerate.SectionType;
import starlight.domain.businessplan.enumerate.SubSectionType;

@Getter
@Entity
@NoArgsConstructor
public class Feasibility extends BaseSection {

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "feasibility_strategy_id", unique = true)
    private SubSection feasibilityStrategy;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "feasibility_market_id", unique = true)
    private SubSection feasibilityMarket;

    public static Feasibility create() {
        return new Feasibility();
    }

    @Override
    protected SectionType getSectionType() {
        return SectionType.GROWTH_STRATEGY;
    }

    @Override
    public SubSection getSubSectionByType(SubSectionType type) {
        return switch (type) {
            case FEASIBILITY_STRATEGY -> this.feasibilityStrategy;
            case FEASIBILITY_MARKET -> this.feasibilityMarket;
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    @Override
    protected void setSubSectionByType(SubSection subSection, SubSectionType type) {
        switch (type) {
            case FEASIBILITY_STRATEGY -> this.feasibilityStrategy = subSection;
            case FEASIBILITY_MARKET -> this.feasibilityMarket = subSection;
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }
}
