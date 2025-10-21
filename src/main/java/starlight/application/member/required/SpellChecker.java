package starlight.application.member.required;

import starlight.adapter.member.spellcheck.dto.Finding;

import java.util.List;

public interface SpellChecker {

    List<Finding> check(String sentence);

    String applyTopSuggestions(String original, List<Finding> findings);
}