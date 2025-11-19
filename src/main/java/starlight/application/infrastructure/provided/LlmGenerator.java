package starlight.application.infrastructure.provided;

import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.List;

public interface LlmGenerator {

    List<Boolean> generateChecklistArray(SubSectionType subSectionType, String content, List<String> criteria, List<String> detailedCriteria);

    String generateReport(String content);
}
