package starlight.application.infrastructure.provided;

import java.util.List;

public interface CheckListGrader {

    List<Boolean> check(String SectionName, String userMsg, int criteriaSize);
}
