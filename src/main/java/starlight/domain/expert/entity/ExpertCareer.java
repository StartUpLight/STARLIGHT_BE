package starlight.domain.expert.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import starlight.shared.AbstractEntity;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "expert_careers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpertCareer extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expert_id", nullable = false)
    private Expert expert;

    @Column(name="order_index", nullable=false)
    private Integer orderIndex;

    @Column(name = "career_title", length = 300, nullable = false)
    private String careerTitle;

    @Column(name = "career_explanation", length = 300)
    private String careerExplanation;

    @Column(name = "career_started_at", nullable = false)
    private LocalDateTime careerStartedAt;

    @Column(name = "career_ended_at", nullable = false)
    private LocalDateTime careerEndedAt;

    public static ExpertCareer of(Expert expert, int orderIndex, String title, String explanation, LocalDateTime startedAt, LocalDateTime endedAt) {
        ExpertCareer expertCareer = new ExpertCareer();
        expertCareer.expert = expert;
        expertCareer.orderIndex = orderIndex;
        expertCareer.careerTitle = title;
        expertCareer.careerExplanation = explanation;
        expertCareer.careerStartedAt = startedAt;
        expertCareer.careerEndedAt = endedAt;
        return expertCareer;
    }

    public void update(int orderIndex, String title, String explanation, LocalDateTime startedAt, LocalDateTime endedAt) {
        this.orderIndex = orderIndex;
        this.careerTitle = title;
        this.careerExplanation = explanation;
        this.careerStartedAt = startedAt;
        this.careerEndedAt = endedAt;
    }
}
