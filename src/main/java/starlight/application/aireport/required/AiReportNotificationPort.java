package starlight.application.aireport.required;

public interface AiReportNotificationPort {

    void sendAiReportCompleted(Long memberId, Long businessPlanId, String businessPlanTitle);
}
