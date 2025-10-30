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
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "overview_basic_id", unique = true)
    private SubSection overviewBasic;

    public static Overview create() {
        Overview overview = new Overview();
        overview.initializeSubSection();

        return overview;
    }

    @SuppressWarnings("deprecation")
    private void initializeSubSection() {
        this.overviewBasic = SubSection.createEmptySubSection(SubSectionName.OVERVIEW_BASIC);
        this.overviewBasic.attachToOverview(this);
    }

    /**
     * 양방향 매핑을 위한 메서드
     */
    public void setSubSectionByType(SubSection subSection) {
        if (subSection.getSubSectionName() == SubSectionName.OVERVIEW_BASIC) {
            this.overviewBasic = subSection;
        }
    }
}
