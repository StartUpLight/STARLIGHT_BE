package starlight.domain.businessplan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import starlight.domain.businessplan.enumerate.SectionType;
import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.Objects;

@Getter
@Entity
@NoArgsConstructor
public class Overview extends BaseSection {

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "overview_basic_id", unique = true)
    private SubSection overviewBasic;

    public static Overview create() {
        return new Overview();
    }

    @Override
    public SubSection getSubSectionByType(SubSectionType type) {
        return switch (type) {
            case OVERVIEW_BASIC -> this.overviewBasic;
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    @Override
    protected void setSubSectionByType(SubSection subSection, SubSectionType type) {
        switch (type) {
            case OVERVIEW_BASIC -> this.overviewBasic = subSection;
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }
}

