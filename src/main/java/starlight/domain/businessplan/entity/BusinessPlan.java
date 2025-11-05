package starlight.domain.businessplan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;
import starlight.domain.businessplan.enumerate.PlanStatus;
import starlight.shared.domain.AbstractEntity;

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

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "businessPlan")
    private Overview overview;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "businessPlan")
    private ProblemRecognition problemRecognition;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "businessPlan")
    private Feasibility feasibility;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "businessPlan")
    private GrowthTactic growthTactic;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "businessPlan")
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

    public void updateStatus(PlanStatus planStatus) {
        this.planStatus = planStatus;
    }

    // 모든 서브 섹션 생성 시에 작성 완료로 판단
    public boolean areWritingCompleted() {
        return overview.areAllSubSectionsCreated()
                && problemRecognition.areAllSubSectionsCreated()
                && feasibility.areAllSubSectionsCreated()
                && growthTactic.areAllSubSectionsCreated()
                && teamCompetence.areAllSubSectionsCreated();
    }

    private void initializeSections() {
        // 공유 기본키 매핑: 자식이 부모와 같은 PK를 사용
        this.overview = Overview.create();
        this.overview.attachBusinessPlan(this);

        this.problemRecognition = ProblemRecognition.create();
        this.problemRecognition.attachBusinessPlan(this);

        this.feasibility = Feasibility.create();
        this.feasibility.attachBusinessPlan(this);

        this.growthTactic = GrowthTactic.create();
        this.growthTactic.attachBusinessPlan(this);

        this.teamCompetence = TeamCompetence.create();
        this.teamCompetence.attachBusinessPlan(this);
    }
}
