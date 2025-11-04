package starlight.domain.businessplan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import starlight.domain.businessplan.enumerate.SubSectionType;
import starlight.shared.domain.BaseEntity;

import java.util.Objects;

@Getter
@MappedSuperclass
@NoArgsConstructor
public abstract class BaseSection extends BaseEntity {

    @Id
    @Column(name = "business_plan_id")
    protected Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "business_plan_id", referencedColumnName = "id")
    protected BusinessPlan businessPlan;

    public void attachBusinessPlan(BusinessPlan businessPlan) {
        this.businessPlan = businessPlan;
    }

    public void putSubSection(SubSection subSection) {
        Objects.requireNonNull(subSection, "subSection must not be null");
        setSubSectionByType(subSection, subSection.getSubSectionType());
    }

    public void removeSubSection(SubSectionType type) {
        setSubSectionByType(null, type);
    }

    public abstract SubSection getSubSectionByType(SubSectionType type);

    protected abstract void setSubSectionByType(SubSection subSection, SubSectionType type);

    protected abstract boolean areAllSubSectionsCreated();
}
