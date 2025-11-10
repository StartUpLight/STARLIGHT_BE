package starlight.application.expertReport;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.expertReport.provided.ExpertReportService;
import starlight.application.expertReport.required.ExpertReportQuery;
import starlight.domain.expertReport.entity.ExpertReport;
import starlight.domain.expertReport.entity.ExpertReportDetail;
import starlight.domain.expertReport.enumerate.SaveType;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpertReportServiceImpl implements ExpertReportService {

    @Value("${feedback-token.token-length}")
    private int TOKEN_LENGTH;

    @Value("${feedback-token.charset}")
    private String BASE62_CHARS;

    @Value("${feedback-token.base-url}")
    private String FEEDBACK_BASE_URL;

    private final ExpertReportQuery expertReportQuery;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public ExpertReport getExpertReport(String token) {
        ExpertReport report = expertReportQuery.findByTokenWithDetails(token);

        report.incrementViewCount();

        return report;
    }

    @Override
    public String createExpertReportLink(
            Long expertId,
            Long businessPlanId
    ) {
        String token = generateToken();

        ExpertReport report = ExpertReport.create(expertId, businessPlanId, token);
        expertReportQuery.save(report);

        return FEEDBACK_BASE_URL + token;
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
            case TEMPORARY -> report.temporarySave();
            case FINAL -> report.submit();
        }

        return expertReportQuery.save(report);
    }

    private String generateToken() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);

        do {
            token.setLength(0);
            for (int i = 0; i < TOKEN_LENGTH; i++) {
                token.append(BASE62_CHARS.charAt(
                        secureRandom.nextInt(BASE62_CHARS.length())
                ));
            }
        } while (expertReportQuery.existsByToken(token.toString()));

        return token.toString();
    }
}