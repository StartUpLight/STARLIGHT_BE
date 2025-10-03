package starlight.application.member.required;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import starlight.adapter.member.spellcheck.DaumSpellChecker;
import starlight.adapter.member.spellcheck.dto.Finding;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SpellCheckerTest {

    @Autowired
    private DaumSpellChecker spellChecker;

    @Test
    void shouldCheckSpellingAndReturnFindings() {
        // given
        String sentence = "안뇽하세요";

        // when
        List<Finding> findings = spellChecker.check(sentence);

        // then
        assertThat(findings).isNotNull();
    }

    @Test
    void shouldApplyTopSuggestions() {
        // given
        String original = "안뇽하세요";
        List<Finding> findings = spellChecker.check(original);

        // when
        String corrected = spellChecker.applyTopSuggestions(original, findings);

        // then
        assertThat(corrected).isNotNull();
        assertThat(corrected).isNotEqualTo(original);
    }
}