package starlight.domain.prompt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import starlight.shared.domain.AbstractEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Prompt extends AbstractEntity {

    @Column(nullable = false)
    private String tag;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}