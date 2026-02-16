package starlight.application.aireport.required;

import starlight.domain.businessplan.entity.BusinessPlan;

public interface BusinessPlanCommandLookupPort {
    BusinessPlan save(BusinessPlan plan);
    
    Long createBusinessPlanWithPdf(String title, String pdfUrl, Long memberId);
}
