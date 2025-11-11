package starlight.domain.businessplan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import starlight.domain.businessplan.enumerate.SubSectionType;

@Getter
@Entity
@NoArgsConstructor
public class GrowthTactic extends BaseSection{

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "growth_model_id", unique = true)
    private SubSection growthModel;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "growth_funding_id", unique = true)
    private SubSection growthFunding;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "growth_entry_id", unique = true)
    private SubSection growthEntry;

    public static GrowthTactic create() {
        return new GrowthTactic();
    }

    @Override
    public SubSection getSubSectionByType(SubSectionType type) {
        return switch (type) {
            case GROWTH_MODEL -> this.growthModel;
            case GROWTH_FUNDING -> this.growthFunding;
            case GROWTH_ENTRY -> this.growthEntry;
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    @Override
    protected void setSubSectionByType(SubSection subSection, SubSectionType type) {
        switch (type) {
            case GROWTH_MODEL -> this.growthModel = subSection;
            case GROWTH_FUNDING -> this.growthFunding = subSection;
            case GROWTH_ENTRY -> this.growthEntry = subSection;
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    @Override
    protected boolean areAllSubSectionsCreated() {
        return this.growthModel != null && this.growthFunding != null && this.growthEntry != null;
    }
}
