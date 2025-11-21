package starlight.application.member.required;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import starlight.adapter.businessplan.spellcheck.DaumSpellChecker;
import starlight.adapter.businessplan.spellcheck.dto.Finding;
import starlight.adapter.businessplan.spellcheck.util.SpellCheckUtil;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DaumSpellCheckerLogicTest {

    @Mock RestClient restClient;
    @Spy SpellCheckUtil spellCheckUtil = new SpellCheckUtil();
    @InjectMocks DaumSpellChecker daumSpellChecker;

    @Test
    @DisplayName("applyTopSuggestions - 최상위 교정 제안 적용")
    void applyTopSuggestions_picksTopCandidate() {

        // given
        String original = "안뇽하세요";
        List<Finding> findings = List.of(
                new Finding("spell","error","안뇽하세요",
                        List.of("안녕하세요","안녕 하세요"),
                        "맞춤법 오류","안뇽하세요","안녕하세요","도움말", List.of())
        );

        // when
        String corrected = daumSpellChecker.applyTopSuggestions(original, findings);

        // then
        assertThat(corrected).isEqualTo("안녕하세요");
    }

    @Test
    @DisplayName("applyTopSuggestions - 교정 제안 없을 시 원문 반환")
    void applyTopSuggestions_returnsOriginalWhenNoFindings() {

        // given
        String original = "안녕하세요";

        // when
        String corrected = daumSpellChecker.applyTopSuggestions(original, List.of());

        // then
        assertThat(corrected).isEqualTo(original);
    }
}

