package starlight.domain.businessplan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import starlight.domain.businessplan.enumerate.SectionType;
import starlight.domain.businessplan.enumerate.SubSectionType;

@Slf4j
@Getter
@Entity
@NoArgsConstructor
public class ProblemRecognition extends BaseSection{

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_background_id")
    private SubSection problemBackground;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_purpose_id")
    private SubSection problemPurpose;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_market_id")
    private SubSection problemMarket;

    public static ProblemRecognition create() {
        return new ProblemRecognition();
    }

    @Override
    public SubSection getSubSectionByType(SubSectionType type) {
        return switch (type) {
            case PROBLEM_BACKGROUND -> this.problemBackground;
            case PROBLEM_PURPOSE -> this.problemPurpose;
            case PROBLEM_MARKET -> this.problemMarket;
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    @Override
    public void setSubSectionByType(SubSection subSection, SubSectionType type) {
        switch (type) {
            case PROBLEM_BACKGROUND -> this.problemBackground = subSection;
            case PROBLEM_PURPOSE -> this.problemPurpose = subSection;
            case PROBLEM_MARKET -> this.problemMarket = subSection;
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }
}
