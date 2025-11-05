package starlight.application.prompt.required;

import java.util.List;

public interface PromptFinder {

    List<String> getSectionCriteria(String tag);
}
