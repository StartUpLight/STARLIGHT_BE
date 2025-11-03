package starlight.application.expertApplication.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import starlight.application.expertApplication.required.EmailSender;
import starlight.application.expertApplication.required.dto.FeedbackRequestEmailDto;

/**
 * 피드백 요청 이벤트 리스너
 * 트랜잭션 커밋 후(AFTER_COMMIT) 비동기로 이메일 전송
 */
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
    public void handleFeedbackRequestEvent(FeedbackRequestEvent event) {
        log.info("Processing feedback request email. expertId={}, planId={}",
                event.getExpert().getId(), event.getPlan().getId());

        try {
            FeedbackRequestEmailDto dto = FeedbackRequestEmailDto.fromDomain(
                    event.getExpert(),
                    event.getMenteeName(),
                    event.getPlan(),
                    event.getFileBytes(),
                    event.getFilename(),
                    event.getFeedbackUrl()
            );

            emailSender.sendFeedbackRequestMail(dto);

            log.info("Successfully sent feedback request email. expertId={}, planId={}",
                    event.getExpert().getId(), event.getPlan().getId());

        } catch (Exception e) {
            log.error("Failed to send feedback request email after retries. expertId={}, planId={}",
                    event.getExpert().getId(), event.getPlan().getId(), e);

            // TODO: 실패 시 처리 전략
            // 1. Dead Letter Queue에 저장
            // 2. 관리자에게 알림 전송
            // 3. DB에 실패 기록 저장
            throw e; // 재시도를 위해 예외 재발생
        }
    }
}