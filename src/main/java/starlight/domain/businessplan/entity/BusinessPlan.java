package starlight.domain.businessplan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;
import starlight.domain.businessplan.enumerate.PlanStatus;
import starlight.shared.AbstractEntity;

@Getter
@Entity
@NoArgsConstructor
public class BusinessPlan extends AbstractEntity {

    @Column(nullable = false)
    private Long memberId;

    @Column(length = 512)
    private String pdfUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PlanStatus planStatus;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "overview_id", unique = true)
    private Overview overview;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "problem_recognition_id", unique = true)
    private ProblemRecognition problemRecognition;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "feasibility_id", unique = true)
    private Feasibility feasibility;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "growth_strategy_id", unique = true)
    private  GrowthStrategy growthStrategy;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "team_competence_id", unique = true)
    private TeamCompetence teamCompetence;

    public static BusinessPlan create(Long memberId, String pdfUrl, PlanStatus planStatus) {
        BusinessPlan businessPlan = new BusinessPlan();
        businessPlan.memberId = memberId;
        businessPlan.pdfUrl = pdfUrl;
        businessPlan.planStatus = (planStatus != null) ? planStatus : PlanStatus.STARTED;

        return businessPlan;
    }

    public void attachOverview(Overview overview) {
        Assert.notNull(overview, "overview must not be null");
        Assert.state(this.overview == null, "Overview already attached");

        this.overview = overview;
    }

    public void detachOverview() {
        Assert.state(this.overview != null, "Overview is not attached");

        this.overview = null;
    }

    public void attachProblemRecognition(ProblemRecognition problemRecognition) {
        Assert.notNull(problemRecognition, "problemRecognition must not be null");
        Assert.state(this.problemRecognition == null, "ProblemRecognition already attached");

        this.problemRecognition = problemRecognition;
    }

    public void detachProblemRecognition() {
        Assert.state(this.problemRecognition != null, "ProblemRecognition is not attached");

        this.problemRecognition = null;
    }

    public void attachFeasibility(Feasibility feasibility) {
        Assert.notNull(feasibility, "feasibility must not be null");
        Assert.state(this.feasibility == null, "Feasibility already attached");

        this.feasibility = feasibility;
    }

    public void detachFeasibility() {
        Assert.state(this.feasibility != null, "Feasibility is not attached");

        this.feasibility = null;
    }

    public void attachGrowthStrategy(GrowthStrategy growthStrategy) {
        Assert.notNull(growthStrategy, "growthStrategy must not be null");
        Assert.state(this.growthStrategy == null, "GrowthStrategy already attached");

        this.growthStrategy = growthStrategy;
    }

    public void detachGrowthStrategy() {
        Assert.state(this.growthStrategy != null, "GrowthStrategy is not attached");

        this.growthStrategy = null;
    }

    public void attachTeamCompetence(TeamCompetence teamCompetence) {
        Assert.notNull(teamCompetence, "teamCompetence must not be null");
        Assert.state(this.teamCompetence == null, "TeamCompetence already attached");

        this.teamCompetence = teamCompetence;
    }

    public void detachTeamCompetence() {
        Assert.state(this.teamCompetence != null, "TeamCompetence is not attached");

        this.teamCompetence = null;
    }
}
