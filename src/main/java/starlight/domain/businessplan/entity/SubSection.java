package starlight.domain.businessplan.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.domain.businessplan.enumerate.SubSectionName;
import starlight.domain.businessplan.value.RawJson;
import starlight.shared.AbstractEntity;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubSection extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feasibility_id")
    private Feasibility feasibility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "overview_id")
    private Overview overview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_recognition_id")
    private ProblemRecognition problemRecognition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "growth_tactic_id")
    private GrowthTactic growthTactic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_competence_id")
    private TeamCompetence teamCompetence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SubSectionName subSectionName;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "raw_json", columnDefinition = "TEXT", nullable = false)
    )
    private RawJson rawJson;

    @Column(nullable = false)
    private boolean checkFirst = false;

    @Column(nullable = false)
    private boolean checkSecond = false;

    @Column(nullable = false)
    private boolean checkThird = false;

    @Column(nullable = false)
    private boolean checkFourth = false;

    @Column(nullable = false)
    private boolean checkFifth = false;

    public static SubSection create(SubSectionName subSectionName, String content, String rawJson) {
        SubSection subSection = new SubSection();
        subSection.subSectionName = subSectionName;
        subSection.content = content;
        subSection.rawJson = RawJson.create(rawJson);
        return subSection;
    }

    public static SubSection createEmptySubSection(SubSectionName subSectionType) {
        return create(subSectionType, "", "");
    }

    public void updateContent(String content, String rawJson) {
        Assert.notNull(content, "content은 null일 수 없습니다.");
        Assert.notNull(rawJson, "rawJson은 null일 수 없습니다.");

        this.content = content;
        this.rawJson = RawJson.create(rawJson);
    }

    public void updateChecks(List<Boolean> checks) {
        Assert.notNull(checks, "checks 리스트는 null일 수 없습니다.");
        Assert.isTrue(checks.size() == 5, "checks 리스트는 길이 5 여야 합니다.");

        applyChecks(checks);
    }

    private void applyChecks(List<Boolean> checks) {
        this.checkFirst = Boolean.TRUE.equals(checks.get(0));
        this.checkSecond = Boolean.TRUE.equals(checks.get(1));
        this.checkThird = Boolean.TRUE.equals(checks.get(2));
        this.checkFourth = Boolean.TRUE.equals(checks.get(3));
        this.checkFifth = Boolean.TRUE.equals(checks.get(4));
    }

    /**
     * SubSectionName의 SectionName을 이용한 양방향 매핑
     */
    public void attachToParentSection(Object parentSection) {
        SectionName sectionName = this.subSectionName.getSection();
        
        switch (sectionName) {
            case OVERVIEW -> {
                this.overview = (Overview) parentSection;
                ((Overview) parentSection).setSubSectionByType(this);
            }
            case PROBLEM_RECOGNITION -> {
                this.problemRecognition = (ProblemRecognition) parentSection;
                ((ProblemRecognition) parentSection).setSubSectionByType(this);
            }
            case FEASIBILITY -> {
                this.feasibility = (Feasibility) parentSection;
                ((Feasibility) parentSection).setSubSectionByType(this);
            }
            case GROWTH_STRATEGY -> {
                this.growthTactic = (GrowthTactic) parentSection;
                ((GrowthTactic) parentSection).setSubSectionByType(this);
            }
            case TEAM_COMPETENCE -> {
                this.teamCompetence = (TeamCompetence) parentSection;
                ((TeamCompetence) parentSection).setSubSectionByType(this);
            }
        }
    }
}
