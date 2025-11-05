package starlight.application.aireport.required;

import starlight.application.aireport.dto.AiReportResponse;
import starlight.domain.businessplan.entity.BusinessPlan;

public interface AiReportGrader {
    AiReportResponse grade(BusinessPlan businessPlan);
}