package starlight.domain.prompt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import starlight.shared.AbstractEntity;

@Entity
@Getter
public class Prompt extends AbstractEntity {

    @Column(nullable = false)
    private String tag;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}