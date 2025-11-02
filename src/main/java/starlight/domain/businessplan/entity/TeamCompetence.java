package starlight.domain.businessplan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import starlight.domain.businessplan.enumerate.SubSectionType;

@Getter
@Entity
@NoArgsConstructor
public class TeamCompetence extends BaseSection{

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "team_founder_id", unique = true)
    private SubSection teamFounder;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "team_members_id", unique = true)
    private SubSection teamMembers;

    public static TeamCompetence create() {
        return new TeamCompetence();
    }

    @Override
    public SubSection getSubSectionByType(SubSectionType type) {
        return switch (type) {
            case TEAM_FOUNDER   -> this.teamFounder;
            case TEAM_MEMBERS -> this.teamMembers;
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    @Override
    public void setSubSectionByType(SubSection subSection, SubSectionType type) {
        switch (type) {
            case TEAM_FOUNDER -> this.teamFounder = subSection;
            case TEAM_MEMBERS -> this.teamMembers = subSection;
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }
}
