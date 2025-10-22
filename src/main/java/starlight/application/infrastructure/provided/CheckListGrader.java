package starlight.application.infrastructure.provided;

import starlight.shared.dto.ClovaResponse;

public interface CheckListGrader {

    ClovaResponse check(String systemMsg, String userMsg, int criteriaSize);
}
