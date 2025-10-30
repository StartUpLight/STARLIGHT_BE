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

    @Column
    private String title;

    @Column(length = 512)
    private String pdfUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PlanStatus planStatus;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "overview_id", unique = true)
    @MapsId
    private Overview overview;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "problem_recognition_id", unique = true)
    @MapsId
    private ProblemRecognition problemRecognition;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "feasibility_id", unique = true)
    @MapsId
    private Feasibility feasibility;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "growth_strategy_id", unique = true)
    @MapsId
    private GrowthTactic growthTactic;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "team_competence_id", unique = true)
    @MapsId
    private TeamCompetence teamCompetence;

    public static BusinessPlan create(Long memberId) {
        Assert.notNull(memberId, "memberId must not be null");

        BusinessPlan businessPlan = new BusinessPlan();
        businessPlan.memberId = memberId;
        businessPlan.planStatus = PlanStatus.STARTED;

        businessPlan.initializeSections();

        return businessPlan;
    }

    public static BusinessPlan createWithPdf(String title, Long memberId, String pdfUrl, PlanStatus planStatus) {
        Assert.notNull(memberId, "memberId must not be null");

        BusinessPlan businessPlan = new BusinessPlan();
        businessPlan.title = title;
        businessPlan.memberId = memberId;
        businessPlan.pdfUrl = pdfUrl;
        businessPlan.planStatus = (planStatus != null) ? planStatus : PlanStatus.STARTED;

        businessPlan.initializeSections();

        return businessPlan;
    }

    public boolean isOwnedBy(Long memberId) {
        return this.memberId.equals(memberId);
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    private void initializeSections() {
        // 각 섹션 엔티티 생성 (@MapsId를 사용하므로 JPA가 자동으로 ID를 설정)
        this.overview = Overview.create();
        this.problemRecognition = ProblemRecognition.create();
        this.feasibility = Feasibility.create();
        this.growthTactic = GrowthTactic.create();
        this.teamCompetence = TeamCompetence.create();
    }
}
