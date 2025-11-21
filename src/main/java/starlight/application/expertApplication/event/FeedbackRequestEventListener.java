package starlight.application.expertApplication.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import starlight.application.expertApplication.required.EmailSender;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedbackRequestEventListener {

    private final EmailSender emailSender;

    @Async("emailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2),
            retryFor = {Exception.class}
    )
    public void handleFeedbackRequestEvent(FeedbackRequestDto event) {
        log.info("[EMAIL] listener triggered menteeName={}, businessPlanTitle={}", event.menteeName(), event.businessPlanTitle());
        try {
            emailSender.sendFeedbackRequestMail(event);

            log.info("[EMAIL] sending via JavaMailSender to={} subject={}", event.mentorEmail(), event.menteeName());

        } catch (Exception e) {
            log.error("[EMAIL] Failed to send feedback request email after retries. menteeName={}, businessPlanTitle={}",
                    event.menteeName(), event.businessPlanTitle(), e);

            throw e;
        }
    }

    @Recover
    public void recoverEmailSend(Exception e, FeedbackRequestDto event) {
        log.error("[EMAIL FINAL FAILURE] ... menteeName={}, businessPlanTitle={}",
                event.menteeName(), event.businessPlanTitle(), e);

        // TODO: 실패 처리 전략
        // 1. Dead Letter Queue에 저장
        // 2. 관리자에게 알림 전송
        // 3. DB에 실패 기록 저장
    }
}