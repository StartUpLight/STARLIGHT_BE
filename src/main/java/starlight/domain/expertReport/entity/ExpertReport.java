package starlight.domain.expertReport.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import starlight.domain.expertReport.enumerate.SubmitStatus;
import starlight.shared.AbstractEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_app_active_one",
                        columnNames = {"business_plan_id", "expert_id"}
                )
        }
)
public class ExpertReport extends AbstractEntity {

    @Column(nullable = false)
    private Long expertId;

    @Column(nullable = false)
    private Long businessPlanId;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Column(unique = true, length = 20)
    private String token;

    @Column
    private Long viewCount = 0L;

    @Column(columnDefinition = "TEXT")
    private String overallComment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubmitStatus submitStatus = SubmitStatus.PENDING;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(nullable = false)
    private List<ExpertReportDetail> details = new ArrayList<>();

    // 7일의 기한을 가지고 기한 내에만 수정가능하다.
        // -> expiredAt, submitStatus 필드로 관리
        // PENDING, TEMPORARY_SAVED 상태에서만 수정 가능
        // PENDING, TEMPORARY_SAVED 상태에서 제출 가능하고, SUBMITTED 상태로 변경
        // SUBMITTED, EXPIRED 상태에서는 수정 불가
        // expiredAt가 현재 시간보다 이전이면 EXPIRED 상태로 변경

    // 기한이 지나거나 제출하면 읽기 전용으로 바뀐다.
        // -> expiredAt, submitStatus 필드로 관리
        // SUBMITTED, EXPIRED 상태에서는 읽기 전용
        // PENDING, TEMPORARY_SAVED 상태에서는 수정 가능
        // expiredAt가 현재 시간보다 이전이면 EXPIRED 상태로 변경

    public static ExpertReport create(Long expertId, Long businessPlanId, String token) {
        ExpertReport expertReport = new ExpertReport();
        expertReport.expertId = expertId;
        expertReport.businessPlanId = businessPlanId;
        expertReport.expiredAt = LocalDateTime.now().plusDays(7);
        expertReport.token = token;
        return expertReport;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    public void temporarySave() {
        validateCanEdit();
        this.submitStatus = SubmitStatus.TEMPORARY_SAVED;
    }

    public void submit() {
        validateCanEdit();
        this.submitStatus = SubmitStatus.SUBMITTED;
    }

    public void syncStatus() {
        if ((submitStatus == SubmitStatus.PENDING ||
                submitStatus == SubmitStatus.TEMPORARY_SAVED)
                && isExpired()) {
            this.submitStatus = SubmitStatus.EXPIRED;
        }
    }

    public void validateCanEdit() {
        syncStatus();

        if (submitStatus == SubmitStatus.SUBMITTED) {
            throw new IllegalStateException("이미 제출된 리포트는 수정할 수 없습니다.");
        }

        if (submitStatus == SubmitStatus.EXPIRED) {
            throw new IllegalStateException("만료된 리포트는 수정할 수 없습니다.");
        }
    }

    public void updateOverallComment(String overallComment) {
        validateCanEdit();
        this.overallComment = overallComment;
    }

    public void updateDetails(List<ExpertReportDetail> newDetails) {
        validateCanEdit();
        this.details.clear();
        this.details.addAll(newDetails);
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public boolean canEdit() {
        syncStatus();
        return submitStatus == SubmitStatus.PENDING ||
                submitStatus == SubmitStatus.TEMPORARY_SAVED;
    }
}
