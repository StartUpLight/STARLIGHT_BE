package starlight.domain.businessplan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import starlight.domain.businessplan.enumerate.SubSectionName;

@Slf4j
@Getter
@Entity
@NoArgsConstructor
public class ProblemRecognition {
    @Id
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_background_id")
    private SubSection problemBackground;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_purpose_id")
    private SubSection problemPurpose;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_market_id")
    private SubSection problemMarket;

    public static ProblemRecognition create() {
        ProblemRecognition problemRecognition = new ProblemRecognition();
        problemRecognition.initializeSubSections();
        return problemRecognition;
    }

    private void initializeSubSections() {
        this.problemBackground = SubSection.createEmptySubSection(SubSectionName.PROBLEM_BACKGROUND);
        this.problemPurpose = SubSection.createEmptySubSection(SubSectionName.PROBLEM_PURPOSE);
        this.problemMarket = SubSection.createEmptySubSection(SubSectionName.PROBLEM_MARKET);
    }
}
