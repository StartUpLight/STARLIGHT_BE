package starlight.domain.expertReport.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;
import starlight.domain.expertReport.enumerate.CommentType;
import starlight.shared.AbstractEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpertReportComment extends AbstractEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CommentType type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    public static ExpertReportComment create(CommentType type, String content) {
        Assert.notNull(type, "type은 필수입니다");
        Assert.hasText(content, "content는 필수입니다");

        ExpertReportComment comment = new ExpertReportComment();
        comment.type = type;
        comment.content = content;
        return comment;
    }

    public void update(String content) {
        this.content = content;
    }
}
