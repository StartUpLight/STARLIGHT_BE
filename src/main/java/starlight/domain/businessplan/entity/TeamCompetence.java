package starlight.domain.businessplan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import starlight.domain.businessplan.enumerate.SubSectionName;

@Getter
@Entity
@NoArgsConstructor
public class TeamCompetence {
    @Id
    @Column(name="business_plan_id")
    private Long id;

    @OneToOne @MapsId
    @JoinColumn(name = "business_plan_id", referencedColumnName = "id")
    private BusinessPlan businessPlan;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "team_founder_id", unique = true)
    private SubSection teamFounder;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "team_members_id", unique = true)
    private SubSection teamMembers;

    public static TeamCompetence create() {
        TeamCompetence teamCompetence = new TeamCompetence();
        return teamCompetence;
    }

    /**
     * 양방향 매핑을 위한 메서드
     */
    public void setSubSectionByType(SubSection subSection) {
        switch (subSection.getSubSectionName()) {
            case TEAM_FOUNDER -> this.teamFounder = subSection;
            case TEAM_MEMBERS -> this.teamMembers = subSection;
        }
    }

    public void attachBusinessPlan(BusinessPlan businessPlan) {
        this.businessPlan = businessPlan;
    }
}
