package starlight.application.expertReport.required;

public interface ExpertReportNotificationPort {

    void sendExpertReportSubmitted(Long memberId, Long businessPlanId, String businessPlanTitle);
}
