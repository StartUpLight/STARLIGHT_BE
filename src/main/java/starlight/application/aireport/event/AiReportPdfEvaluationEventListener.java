package starlight.application.aireport.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import starlight.application.aireport.AiReportService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiReportPdfEvaluationEventListener {

    private final AiReportService aiReportService;

    @Async("aiReportPdfExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPdfReportRequested(PdfReportRequestedInput event) {
        aiReportService.handlePdfReportRequested(event.businessPlanId(), event.pdfUrl(), event.memberId());
    }
}
