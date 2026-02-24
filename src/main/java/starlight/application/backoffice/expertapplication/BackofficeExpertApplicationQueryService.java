package starlight.application.backoffice.expertapplication;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.backoffice.expertapplication.provided.BackofficeExpertApplicationQueryUseCase;
import starlight.application.backoffice.expertapplication.provided.dto.result.BackofficeBusinessPlanExpertApplicationsResult;
import starlight.application.backoffice.expertapplication.required.BackofficeExpertApplicationQueryPort;
import starlight.application.backoffice.expertapplication.required.BusinessPlanLookupPort;
import starlight.application.backoffice.expertapplication.required.ExpertLookupPort;
import starlight.application.backoffice.expertapplication.required.ExpertReportLookupPort;
import starlight.application.backoffice.expertapplication.required.dto.BackofficeExpertApplicationLookupResult;
import starlight.domain.expertReport.entity.ExpertReport;
import starlight.domain.expertReport.entity.ExpertReportComment;
import starlight.domain.expertReport.enumerate.CommentType;
import starlight.domain.expertReport.enumerate.SubmitStatus;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BackofficeExpertApplicationQueryService implements BackofficeExpertApplicationQueryUseCase {

    private final BusinessPlanLookupPort businessPlanLookupPort;
    private final BackofficeExpertApplicationQueryPort expertApplicationQueryPort;
    private final ExpertLookupPort expertLookupPort;
    private final ExpertReportLookupPort expertReportLookupPort;

    @Override
    public BackofficeBusinessPlanExpertApplicationsResult findByBusinessPlanId(Long businessPlanId) {
        businessPlanLookupPort.findByIdOrThrow(businessPlanId);

        List<BackofficeExpertApplicationLookupResult> applications =
                expertApplicationQueryPort.findByBusinessPlanId(businessPlanId);
        Map<Long, String> expertNameMap = expertLookupPort.findExpertNamesByIds(
                applications.stream().map(BackofficeExpertApplicationLookupResult::expertId).toList()
        );
        Map<Long, ExpertReport> reportMap = buildExpertReportMap(businessPlanId);

        List<BackofficeBusinessPlanExpertApplicationsResult.ExpertApplicationResult> content = applications.stream()
                .map(application -> {
                    ExpertReport report = reportMap.get(application.expertId());
                    SubmitStatus submitStatus = report != null ? report.getSubmitStatus() : application.submitStatus();

                    return BackofficeBusinessPlanExpertApplicationsResult.ExpertApplicationResult.of(
                            application.applicationId(),
                            application.expertId(),
                            expertNameMap.get(application.expertId()),
                            toStatusCode(submitStatus),
                            application.requestedAt(),
                            report != null ? report.getModifiedAt() : application.feedbackUpdatedAt(),
                            null,
                            report != null ? report.getOverallComment() : application.feedbackSummary(),
                            extractCommentsByType(report, CommentType.STRENGTH),
                            extractCommentsByType(report, CommentType.WEAKNESS)
                    );
                })
                .toList();

        return BackofficeBusinessPlanExpertApplicationsResult.of(businessPlanId, content);
    }

    private String toStatusCode(SubmitStatus submitStatus) {
        if (submitStatus == null) {
            return "REQUESTED";
        }

        return switch (submitStatus) {
            case SUBMITTED -> "FEEDBACK_COMPLETED";
            case PENDING, TEMPORARY_SAVED, EXPIRED -> "FEEDBACK_PENDING";
        };
    }

    private Map<Long, ExpertReport> buildExpertReportMap(Long businessPlanId) {
        List<ExpertReport> reports = expertReportLookupPort.findAllByBusinessPlanIdWithCommentsOrderByCreatedAtDesc(
                businessPlanId
        );
        Map<Long, ExpertReport> map = new LinkedHashMap<>();
        for (ExpertReport report : reports) {
            map.putIfAbsent(report.getExpertId(), report);
        }
        return map;
    }

    private List<String> extractCommentsByType(ExpertReport report, CommentType type) {
        if (report == null || report.getComments() == null || report.getComments().isEmpty()) {
            return Collections.emptyList();
        }

        return report.getComments().stream()
                .filter(comment -> comment.getType() == type)
                .map(ExpertReportComment::getContent)
                .toList();
    }
}
