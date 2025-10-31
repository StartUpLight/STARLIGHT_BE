package starlight.domain.businessplan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import starlight.domain.businessplan.enumerate.SubSectionName;

@Getter
@Entity
@NoArgsConstructor
public class GrowthTactic {
    @Id
    @Column(name="business_plan_id")
    private Long id;

    @OneToOne @MapsId
    @JoinColumn(name = "business_plan_id", referencedColumnName = "id")
    private BusinessPlan businessPlan;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "growth_model_id", unique = true)
    private SubSection growthModel;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "growth_funding_id", unique = true)
    private SubSection growthFunding;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "growth_entry_id", unique = true)
    private SubSection growthEntry;

    public static GrowthTactic create() {
        GrowthTactic growthTactic = new GrowthTactic();
        return growthTactic;
    }

    /**
     * 양방향 매핑을 위한 메서드
     */
    public void setSubSectionByType(SubSection subSection) {
        switch (subSection.getSubSectionName()) {
            case GROWTH_MODEL -> this.growthModel = subSection;
            case GROWTH_FUNDING -> this.growthFunding = subSection;
            case GROWTH_ENTRY -> this.growthEntry = subSection;
        }
    }

    public void attachBusinessPlan(BusinessPlan businessPlan) {
        this.businessPlan = businessPlan;
    }
}
