package starlight.application.infrastructure.provided;

import java.util.List;

public interface LlmGenerator {

    List<Boolean> generateChecklistArray(String newContent, List<String> criteria, String previousContent, List<Boolean> previousChecks);

    String generateReport(String content);
}
