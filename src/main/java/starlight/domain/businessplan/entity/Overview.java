package starlight.domain.businessplan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import starlight.domain.businessplan.enumerate.SubSectionName;

@Getter
@Entity
@NoArgsConstructor
public class Overview {
    @Id
    @Column(name="business_plan_id")
    private Long id;

    @OneToOne @MapsId
    @JoinColumn(name = "business_plan_id", referencedColumnName = "id")
    private BusinessPlan businessPlan;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "overview_basic_id", unique = true)
    private SubSection overviewBasic;

    public static Overview create() {
        Overview overview = new Overview();
        return overview;
    }

    /**
     * 양방향 매핑을 위한 메서드
     */
    public void setSubSectionByType(SubSection subSection) {
        if (subSection.getSubSectionName() == SubSectionName.OVERVIEW_BASIC) {
            this.overviewBasic = subSection;
        }
    }

    public void attachBusinessPlan(BusinessPlan businessPlan) {
        this.businessPlan = businessPlan;
    }
}
