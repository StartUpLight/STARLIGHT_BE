package starlight.application.spellcheck.required;

import starlight.adapter.spellcheck.spellcheck.dto.Finding;

import java.util.List;

public interface SpellChecker {

    /**
     * 맞춤법 검사
     * @param sentence
     * @return
     */
    List<Finding> check(String sentence);

    /**
     * 최상위 교정 제안 적용(교정문)
     * @param original
     * @param findings
     * @return
     */
    String applyTopSuggestions(String original, List<Finding> findings);
}