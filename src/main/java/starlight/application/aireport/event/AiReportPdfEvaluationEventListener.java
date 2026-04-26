package starlight.application.aireport.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import starlight.application.aireport.AiReportReadyMailInput;
import starlight.application.aireport.AiReportService;
import starlight.application.aireport.required.AiReportMailPort;
import starlight.application.aireport.required.BusinessPlanQueryLookupPort;
import starlight.application.aireport.required.PdfDownloadPort;
import starlight.application.member.required.MemberQueryPort;
import starlight.domain.businessplan.entity.BusinessPlan;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiReportPdfEvaluationEventListener {

    private final AiReportService aiReportService;
    private final MemberQueryPort memberQueryPort;
    private final BusinessPlanQueryLookupPort businessPlanQueryLookupPort;
    private final PdfDownloadPort pdfDownloadPort;
    private final AiReportMailPort aiReportMailPort;

    @Async("aiReportPdfExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPdfReportRequested(PdfReportRequestedEvent event) {
        long planId = event.businessPlanId();
        try {
            aiReportService.completePdfGrading(planId, event.pdfUrl(), event.memberId());
        } catch (Exception e) {
            log.error("[AI_REPORT_PDF] grading failed. businessPlanId={}, memberId={}", planId, event.memberId(), e);
            return;
        }

        try {
            var member = memberQueryPort.findByIdOrThrow(event.memberId());
            BusinessPlan plan = businessPlanQueryLookupPort.findByIdOrThrow(planId);
            byte[] pdfBytes = pdfDownloadPort.downloadFromUrl(event.pdfUrl());
            String safeTitle = plan.getTitle() == null ? "business-plan" : plan.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_");
            String filename = safeTitle + ".pdf";
            String reportUrl = aiReportService.buildAiReportWebUrl(planId);
            aiReportMailPort.sendPdfAiReportReadyMail(
                    AiReportReadyMailInput.of(member.getEmail(), member.getName(), reportUrl, filename, pdfBytes));
            log.info("[AI_REPORT_PDF] completion mail sent. businessPlanId={}, to={}", planId, member.getEmail());
        } catch (Exception e) {
            log.error("[AI_REPORT_PDF] completion mail or PDF download failed. businessPlanId={}", planId, e);
        }
    }
}
