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

    @Column(name = "parent_section_id")
    private Long parentSectionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "parent_section_type", length = 50)
    private SectionName parentSectionName;

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

    @Getter
    private static final int CHECKLIST_SIZE = 5;

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
        Assert.isTrue(checks.size() == CHECKLIST_SIZE, "checks 리스트는 길이 5 여야 합니다.");

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
     * 부모 섹션에 연결 (단일 parent 참조 방식)
     * 
     * @param parentSectionId   부모 섹션 ID (Overview, ProblemRecognition 등의 ID)
     * @param parentSectionType 부모 섹션 타입
     */
    public void attachToParent(Long parentSectionId, SectionName parentSectionType) {
        Assert.notNull(parentSectionId, "parentSectionId는 null일 수 없습니다.");
        Assert.notNull(parentSectionType, "parentSectionType은 null일 수 없습니다.");

        this.parentSectionId = parentSectionId;
        this.parentSectionName = parentSectionType;
    }
}
