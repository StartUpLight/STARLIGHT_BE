package starlight.application.aireport.required;

import starlight.application.aireport.event.AiReportReadyMailInput;

public interface AiReportMailPort {

    void sendPdfAiReportReadyMail(AiReportReadyMailInput input);
}
