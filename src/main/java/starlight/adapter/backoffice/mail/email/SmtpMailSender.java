package starlight.adapter.backoffice.mail.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import starlight.application.backoffice.mail.provided.dto.input.BackofficeMailSendInput;
import starlight.application.backoffice.mail.required.MailSenderPort;
import starlight.domain.backoffice.mail.BackofficeMailContentType;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpMailSender implements MailSenderPort {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public void send(BackofficeMailSendInput input, BackofficeMailContentType contentType) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(input.to().toArray(new String[0]));
            helper.setSubject(input.subject());

            boolean isHtml = contentType == BackofficeMailContentType.HTML;
            String body = isHtml ? input.html() : input.text();
            helper.setText(body, isHtml);

            javaMailSender.send(message);
            log.info("[MAIL] sent to={} subject={}", input.to(), input.subject());
        } catch (MessagingException e) {
            log.error("[MAIL] send failed to={}", input.to(), e);
            throw new IllegalArgumentException("메일 전송 실패");
        }
    }
}
