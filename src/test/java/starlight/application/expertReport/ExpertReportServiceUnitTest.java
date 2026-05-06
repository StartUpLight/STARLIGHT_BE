package starlight.application.expertReport;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import starlight.application.businessplan.required.BusinessPlanQueryPort;
import starlight.application.expertReport.required.ExpertApplicationCountLookupPort;
import starlight.application.expertReport.required.ExpertLookupPort;
import starlight.application.expertReport.required.ExpertReportCommandPort;
import starlight.application.expertReport.required.ExpertReportQueryPort;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.PlanStatus;
import starlight.domain.expertReport.entity.ExpertReport;
import starlight.domain.expertReport.enumerate.SaveType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExpertReportServiceUnitTest {

    private final ExpertReportQueryPort expertReportQuery = mock(ExpertReportQueryPort.class);
    private final ExpertReportCommandPort expertReportCommand = mock(ExpertReportCommandPort.class);
    private final ExpertLookupPort expertLookupPort = mock(ExpertLookupPort.class);
    private final ExpertApplicationCountLookupPort expertApplicationLookupPort =
            mock(ExpertApplicationCountLookupPort.class);
    private final BusinessPlanQueryPort businessPlanQuery = mock(BusinessPlanQueryPort.class);

    private final ExpertReportService sut = new ExpertReportService(
            expertReportQuery,
            expertReportCommand,
            expertLookupPort,
            expertApplicationLookupPort,
            businessPlanQuery
    );

    @Test
    void 최종제출이면_사업계획서를_최종화한다() {
        ExpertReport report = ExpertReport.create(2L, 10L, "token");
        BusinessPlan plan = businessPlan();

        when(expertReportQuery.findByTokenWithCommentsOrThrow("token")).thenReturn(report);
        when(businessPlanQuery.findByIdOrThrow(10L)).thenReturn(plan);
        when(expertReportCommand.save(report)).thenReturn(report);

        ExpertReport savedReport = sut.saveReport("token", "overall", List.of(), SaveType.FINAL);

        assertThat(savedReport).isSameAs(report);
        assertThat(plan.getPlanStatus()).isEqualTo(PlanStatus.FINALIZED);
    }

    @Test
    void 임시저장이면_사업계획서를_최종화하지_않는다() {
        ExpertReport report = ExpertReport.create(2L, 10L, "token");

        when(expertReportQuery.findByTokenWithCommentsOrThrow("token")).thenReturn(report);
        when(expertReportCommand.save(report)).thenReturn(report);

        ExpertReport savedReport = sut.saveReport("token", "overall", List.of(), SaveType.TEMPORARY);

        assertThat(savedReport).isSameAs(report);
    }

    private BusinessPlan businessPlan() {
        BusinessPlan plan = BusinessPlan.create("사업계획서", 1L);
        ReflectionTestUtils.setField(plan, "id", 10L);
        return plan;
    }
}
