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
public class ExpertReportDetail extends AbstractEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CommentType commentType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    public static ExpertReportDetail create(CommentType commentType, String content) {
        Assert.notNull(commentType, "commentType은 필수입니다");
        Assert.hasText(content, "content는 필수입니다");

        ExpertReportDetail detail = new ExpertReportDetail();
        detail.commentType = commentType;
        detail.content = content;
        return detail;
    }

    public void update(String content) {
        this.content = content;
    }
}
