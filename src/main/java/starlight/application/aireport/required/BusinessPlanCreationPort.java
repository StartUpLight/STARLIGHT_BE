package starlight.application.aireport.required;

public interface BusinessPlanCreationPort {

    Long createBusinessPlanWithPdf(String title, String pdfUrl, Long memberId);
}

