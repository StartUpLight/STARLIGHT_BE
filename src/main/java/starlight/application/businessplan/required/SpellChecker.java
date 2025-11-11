package starlight.application.businessplan.required;

import starlight.adapter.businessplan.spellcheck.dto.Finding;

import java.util.List;

public interface SpellChecker {

    List<Finding> check(String sentence);

    String applyTopSuggestions(String original, List<Finding> findings);
}