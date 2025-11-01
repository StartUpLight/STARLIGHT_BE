package starlight.domain.expertApplication.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import starlight.domain.expertApplication.enumerate.ApplicationStatus;
import starlight.shared.AbstractEntity;

@Getter
@ToString
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(
        name = "mentor_application",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_app_active_one",
                        columnNames = {"business_plan_id", "mentor_id", "active_flag"}
                )
        }
)
public class expertApplication extends AbstractEntity {

    @Column(nullable = false)
    private Long businessPlanId;

    @Column(nullable = false)
    private Long mentorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    /**
     * 조건부 유니크 구현용 보조 컬럼
     * - REQUESTED → TRUE
     * - 그 외     → NULL  (중요: false를 쓰지 말 것)
     */
    @Column(name = "active_flag")
    private Boolean activeFlag;
}
