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
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "team_founder_id", unique = true)
    private SubSection teamFounder;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "team_members_id", unique = true)
    private SubSection teamMembers;

    public static TeamCompetence create() {
        TeamCompetence teamCompetence = new TeamCompetence();
        teamCompetence.initializeSubSections();
        return teamCompetence;
    }

    @SuppressWarnings("deprecation")
    private void initializeSubSections() {
        this.teamFounder = SubSection.createEmptySubSection(SubSectionName.TEAM_FOUNDER);
        this.teamFounder.attachToTeamCompetence(this);
        
        this.teamMembers = SubSection.createEmptySubSection(SubSectionName.TEAM_MEMBERS);
        this.teamMembers.attachToTeamCompetence(this);
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
}
