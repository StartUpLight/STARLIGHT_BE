package starlight.adapter.businessplan.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import starlight.application.expert.required.EmailSender;
import starlight.application.expert.required.FeedbackRequestEmailDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class SMTPEmailSender implements EmailSender {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public void sendFeedbackRequestMail(FeedbackRequestEmailDto dto) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(dto.mentorEmail());
            helper.setSubject("[피드백 요청] " + dto.menteeName() + "의 사업계획서 검토 요청");

            Context ctx = new Context();
            ctx.setVariable("mentorName", dto.mentorName());
            ctx.setVariable("studentName", dto.menteeName());
            ctx.setVariable("planTitle", dto.businessPlanTitle());
            ctx.setVariable("feedbackDeadline", dto.feedbackDeadline());
            ctx.setVariable("feedbackUrl", dto.feedbackUrl());

            String htmlContent = templateEngine.process("feedback-request", ctx);
            helper.setText(htmlContent, true);

            if (dto.attachedFile() != null && dto.filename() != null) {
                helper.addAttachment(dto.filename(), new ByteArrayResource(dto.attachedFile()));
            }

            javaMailSender.send(message);
            log.info("피드백 요청 메일 발송 성공 - To: {}", dto.mentorEmail());

        } catch (MessagingException e) {
            log.error("피드백 요청 메일 발송 실패 - To: {}", dto.mentorEmail(), e);
            throw new RuntimeException("피드백 요청 메일 발송 실패", e);
        }
    }
}
