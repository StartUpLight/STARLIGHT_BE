package starlight.adapter.shared.infrastructure.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import starlight.application.aireport.AiReportReadyMailInput;
import starlight.application.aireport.required.AiReportMailPort;
import starlight.application.backoffice.mail.provided.dto.input.BackofficeMailSendInput;
import starlight.application.backoffice.mail.required.BackofficeMailPort;
import starlight.application.expertApplication.event.FeedbackRequestInput;
import starlight.application.expertApplication.required.FeedbackRequestMailPort;
import starlight.domain.backoffice.exception.BackofficeErrorType;
import starlight.domain.backoffice.exception.BackofficeException;
import starlight.domain.backoffice.mail.BackofficeMailContentType;
import starlight.domain.expertApplication.exception.ExpertApplicationErrorType;
import starlight.domain.expertApplication.exception.ExpertApplicationException;

@Slf4j
@Component
public class SmtpMailClient implements BackofficeMailPort,
        FeedbackRequestMailPort,
        AiReportMailPort {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public SmtpMailClient(JavaMailSender javaMailSender, SpringTemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

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
            log.info("[MAIL] sent recipients={} subject={}", input.to().size(), input.subject());
        } catch (MessagingException e) {
            log.error("[MAIL] send failed recipients={}", input.to().size(), e);
            throw new BackofficeException(BackofficeErrorType.MAIL_SEND_FAILED, e);
        }
    }

    @Override
    public void sendFeedbackRequestMail(FeedbackRequestInput input) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(input.mentorEmail());
            helper.setSubject("[STARLIGHT] " + input.menteeName() + "의 사업계획서 검토 요청");

            Context ctx = new Context();
            ctx.setVariable("mentorName", input.mentorName());
            ctx.setVariable("studentName", input.menteeName());
            ctx.setVariable("planTitle", input.businessPlanTitle());
            ctx.setVariable("feedbackDeadline", input.feedbackDeadline());
            ctx.setVariable("feedbackUrl", input.feedbackUrl());

            String htmlContent = templateEngine.process("feedback-request", ctx);
            helper.setText(htmlContent, true);

            if (input.attachedFile() != null && input.filename() != null) {
                helper.addAttachment(input.filename(), new ByteArrayResource(input.attachedFile()));
            }

            javaMailSender.send(message);
            log.info("피드백 요청 메일 발송 성공 - To: {}", input.mentorEmail());

        } catch (MessagingException e) {
            log.error("피드백 요청 메일 발송 실패 - To: {}", input.mentorEmail(), e);
            throw new ExpertApplicationException(ExpertApplicationErrorType.EMAIL_SEND_ERROR);
        }
    }

    @Override
    public void sendPdfAiReportReadyMail(AiReportReadyMailInput input) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(input.toEmail());
            helper.setSubject("[STARLIGHT] \"" + input.filename() + "\"에 대한 AI 리포트가 도착했습니다");

            Context ctx = new Context();
            ctx.setVariable("name", input.recipientName());
            ctx.setVariable("reportUrl", input.reportUrl());
            String htmlContent = templateEngine.process("pdf-ai-report-ready", ctx);
            helper.setText(htmlContent, true);
            helper.addAttachment(input.filename(), new ByteArrayResource(input.pdfBytes()));

            javaMailSender.send(message);
            log.info("[MAIL] AI 리포트 완료 메일 발송 to={}", input.toEmail());
        } catch (MessagingException e) {
            log.error("[MAIL] AI 리포트 완료 메일 발송 실패 to={}", input.toEmail(), e);
        } catch (Exception e) {
            log.error("[MAIL] AI 리포트 완료 메일 처리 실패 to={}", input.toEmail(), e);
        }
    }
}
