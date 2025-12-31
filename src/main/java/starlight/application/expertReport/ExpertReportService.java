package starlight.application.expertReport;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.application.expertReport.provided.ExpertReportServiceUseCase;
import starlight.application.expertReport.provided.dto.ExpertReportWithExpertResult;
import starlight.application.expertReport.required.ExpertApplicationCountLookupPort;
import starlight.application.expertReport.required.ExpertLookupPort;
import starlight.application.expertReport.required.ExpertReportCommandPort;
import starlight.application.expertReport.required.ExpertReportQueryPort;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.PlanStatus;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.exception.ExpertErrorType;
import starlight.domain.expert.exception.ExpertException;
import starlight.domain.expertReport.entity.ExpertReport;
import starlight.domain.expertReport.entity.ExpertReportComment;
import starlight.domain.expertReport.enumerate.SaveType;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpertReportService implements ExpertReportServiceUseCase {

    @Value("${feedback-token.token-length}")
    private int tokenLength;

    @Value("${feedback-token.charset}")
    private String base62Chars;

    @Value("${feedback-token.base-url}")
    private String feedbackBaseUrl;

    private final ExpertReportQueryPort expertReportQuery;
    private final ExpertReportCommandPort expertReportCommand;
    private final ExpertLookupPort expertLookupPort;
    private final ExpertApplicationCountLookupPort expertApplicationLookupPort;
    private final BusinessPlanQuery businessPlanQuery;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String createExpertReportLink(
            Long expertId,
            Long businessPlanId
    ) {
        String token = generateToken();

        ExpertReport report = ExpertReport.create(expertId, businessPlanId, token);
        expertReportCommand.save(report);

        return feedbackBaseUrl + token;
    }

    @Override
    public ExpertReport saveReport(
            String token,
            String overallComment,
            List<ExpertReportComment> comments,
            SaveType saveType
    ) {
        ExpertReport report = expertReportQuery.findByTokenWithCommentsOrThrow(token);

        report.updateOverallComment(overallComment);
        report.updateComments(comments);

        switch (saveType) {
            case TEMPORARY -> {
                report.temporarySave();
            }
            case FINAL -> {
                report.submit();
                BusinessPlan plan = businessPlanQuery.findByIdOrThrow(report.getBusinessPlanId());
                plan.updateStatus(PlanStatus.FINALIZED);
            }

        }

        return expertReportCommand.save(report);
    }

    @Override
    public ExpertReportWithExpertResult getExpertReportWithExpert(String token) {
        ExpertReport report = expertReportQuery.findByTokenWithCommentsOrThrow(token);
        report.incrementViewCount();

        Expert expert = expertLookupPort.findByIdWithCareersAndTags(report.getExpertId());

        Map<Long, Long> countMap = expertApplicationLookupPort.countByExpertIds(List.of(report.getExpertId()));
        Long applicationCount = countMap.getOrDefault(report.getExpertId(), 0L);

        return ExpertReportWithExpertResult.of(report, expert, applicationCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpertReportWithExpertResult> getExpertReportsWithExpertByBusinessPlanId(Long businessPlanId) {
        businessPlanQuery.findByIdOrThrow(businessPlanId);

        List<ExpertReport> reports = expertReportQuery.findAllByBusinessPlanIdWithCommentsOrderByCreatedAtDesc(
                businessPlanId
        );

        Set<Long> expertIds = reports.stream()
                .map(ExpertReport::getExpertId)
                .collect(Collectors.toSet());

        Map<Long, Expert> expertsMap = expertLookupPort.findByIds(expertIds);
        if (!expertIds.isEmpty() && expertsMap.size() != expertIds.size()) {
            throw new ExpertException(ExpertErrorType.EXPERT_NOT_FOUND);
        }

        Map<Long, Long> countMap = expertApplicationLookupPort.countByExpertIds(expertIds.stream().toList());

        return reports.stream()
                .map(report -> {
                    Expert expert = expertsMap.get(report.getExpertId());
                    Long applicationCount = countMap.getOrDefault(report.getExpertId(), 0L);
                    return ExpertReportWithExpertResult.of(report, expert, applicationCount);
                })
                .toList();
    }

    private String generateToken() {
        StringBuilder token = new StringBuilder(tokenLength);

        do {
            token.setLength(0);
            for (int i = 0; i < tokenLength; i++) {
                token.append(base62Chars.charAt(
                        secureRandom.nextInt(base62Chars.length())
                ));
            }
        } while (expertReportQuery.existsByToken(token.toString()));

        return token.toString();
    }
}
