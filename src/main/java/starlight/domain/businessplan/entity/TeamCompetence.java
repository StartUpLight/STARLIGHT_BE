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
    @JoinColumn(name = "team_founder_id")
    private SubSection teamFounder;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "team_members_id")
    private SubSection teamMembers;

    public static TeamCompetence create() {
        TeamCompetence teamCompetence = new TeamCompetence();
        teamCompetence.initializeSubSections();
        return teamCompetence;
    }

    private void initializeSubSections() {
        this.teamFounder = SubSection.createEmptySubSection(SubSectionName.TEAM_FOUNDER);
        this.teamMembers = SubSection.createEmptySubSection(SubSectionName.TEAM_MEMBERS);
    }
}
