package starlight.application.aireport.required;

import starlight.domain.aireport.entity.AiReport;

public interface AiReportCommandPort {

    AiReport save(AiReport aiReport);
}
