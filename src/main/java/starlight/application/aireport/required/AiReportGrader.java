package starlight.application.aireport.required;

import starlight.application.aireport.provided.dto.AiReportResponse;

public interface AiReportGrader {
    AiReportResponse gradeContent(String content);
}