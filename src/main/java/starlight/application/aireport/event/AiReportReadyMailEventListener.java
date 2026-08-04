package starlight.application.aireport.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import starlight.application.aireport.required.AiReportMailPort;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiReportReadyMailEventListener {

    private final AiReportMailPort aiReportMailPort;

    @Async("emailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2),
            retryFor = {Exception.class}
    )
    public void handleAiReportReadyMailEvent(AiReportReadyMailInput event) {
        log.info("[AI_REPORT_PDF] mail listener triggered to={} filename={}", event.toEmail(), event.filename());
        try {
            aiReportMailPort.sendPdfAiReportReadyMail(event);
            log.info("[AI_REPORT_PDF] completion mail sent to={}", event.toEmail());
        } catch (Exception e) {
            log.error("[AI_REPORT_PDF] completion mail failed to={} filename={}", event.toEmail(), event.filename(), e);
            throw e;
        }
    }

    @Recover
    public void recoverAiReportReadyMail(Exception e, AiReportReadyMailInput event) {
        log.error("[AI_REPORT_PDF FINAL FAILURE] to={} filename={}", event.toEmail(), event.filename(), e);
    }
}
