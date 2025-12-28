package starlight.application.expertReport;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.application.expertReport.provided.ExpertReportServiceUseCase;
import starlight.application.expertReport.provided.dto.ExpertReportWithExpertDto;
import starlight.application.expertReport.required.ExpertLookupPort;
import starlight.application.expertReport.required.ExpertReportCommandPort;
import starlight.application.expertReport.required.ExpertReportQueryPort;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.PlanStatus;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expertReport.entity.ExpertReport;
import starlight.domain.expertReport.entity.ExpertReportDetail;
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
            List<ExpertReportDetail> details,
            SaveType saveType
    ) {
        ExpertReport report = expertReportQuery.findByTokenWithDetails(token);

        report.updateOverallComment(overallComment);
        report.updateDetails(details);

        switch (saveType) {
            case TEMPORARY -> {
                report.temporarySave();
            }
            case FINAL -> {
                report.submit();
                BusinessPlan plan = businessPlanQuery.getOrThrow(report.getBusinessPlanId());
                plan.updateStatus(PlanStatus.FINALIZED);
            }

        }

        return expertReportCommand.save(report);
    }

    @Override
    public ExpertReportWithExpertDto getExpertReportWithExpert(String token) {
        ExpertReport report = expertReportQuery.findByTokenWithDetails(token);
        report.incrementViewCount();

        Expert expert = expertLookupPort.findByIdWithCareersAndTags(report.getExpertId());

        return ExpertReportWithExpertDto.of(report, expert);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpertReportWithExpertDto> getExpertReportsWithExpertByBusinessPlanId(Long businessPlanId) {
        List<ExpertReport> reports = expertReportQuery.findAllByBusinessPlanIdOrderByCreatedAtDesc(businessPlanId);

        Set<Long> expertIds = reports.stream()
                .map(ExpertReport::getExpertId)
                .collect(Collectors.toSet());

        Map<Long, Expert> expertsMap = expertLookupPort.findByIds(expertIds);

        return reports.stream()
                .map(report -> {
                    Expert expert = expertsMap.get(report.getExpertId());
                    return ExpertReportWithExpertDto.of(report, expert);
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
