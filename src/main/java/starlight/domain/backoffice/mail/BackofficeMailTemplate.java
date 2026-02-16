package starlight.domain.backoffice.mail;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;
import starlight.shared.AbstractEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BackofficeMailTemplate extends AbstractEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String emailTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BackofficeMailContentType contentType;

    @Column(columnDefinition = "TEXT")
    private String html;

    @Column(columnDefinition = "TEXT")
    private String text;

    public static BackofficeMailTemplate create(
            String name, String emailTitle, BackofficeMailContentType contentType, String html, String text
    ) {
        Assert.hasText(name, "name must not be empty");
        Assert.hasText(emailTitle, "title must not be empty");
        Assert.notNull(contentType, "contentType must not be null");

        BackofficeMailTemplate template = new BackofficeMailTemplate();
        template.name = name;
        template.emailTitle = emailTitle;
        template.contentType = contentType;
        template.html = html;
        template.text = text;

        return template;
    }
}
