package starlight.application.aireport.required;

import starlight.domain.businessplan.entity.BusinessPlan;

public interface BusinessPlanCommandLookUpPort {
    BusinessPlan save(BusinessPlan plan);
    
    Long createBusinessPlanWithPdf(String title, String pdfUrl, Long memberId);
}
