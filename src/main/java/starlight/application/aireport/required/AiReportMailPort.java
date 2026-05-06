package starlight.application.aireport.required;

import starlight.application.aireport.AiReportReadyMailInput;

public interface AiReportMailPort {

    void sendPdfAiReportReadyMail(AiReportReadyMailInput input);
}
