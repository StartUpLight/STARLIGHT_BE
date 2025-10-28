package starlight.adapter.businessplan.spellcheck;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import starlight.adapter.businessplan.spellcheck.dto.Finding;
import starlight.adapter.businessplan.spellcheck.util.SpellCheckUtil;
import starlight.application.businessplan.required.SpellChecker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DaumSpellChecker implements SpellChecker {

    private static final int  MAX_CHARS            = 1000;  // 요청 글자 수 제한
    private static final long DAUM_MIN_INTERVAL_MS = 400L;  // 호출 간 최소 간격

    private final RestClient spellCheckClient;
    private final SpellCheckUtil spellCheckUtil;

    public String applyTopSuggestions(String original, List<Finding> findings) {

        String fixed = original;
        if (findings == null || findings.isEmpty()) return fixed;

        List<Finding> sorted = new ArrayList<>(findings);
        sorted.sort(Comparator.comparingInt((Finding f) -> f.token() != null ? f.token().length() : 0)
                .reversed());

        for (Finding f : sorted) {
            if (f.token() != null && f.suggestions() != null && !f.suggestions().isEmpty()) {
                fixed = fixed.replace(f.token(), f.suggestions().get(0));
            }
        }
        return fixed;
    }

    public List<Finding> check(String sentence) {

        List<String> parts = spellCheckUtil.splitByLength(sentence, ".,\n", MAX_CHARS);

        if (parts.isEmpty()) {
            return List.of();
        }

        List<Finding> checkedSpells = new ArrayList<>(parts.size() * 8);

        for (int i = 0; i < parts.size(); i++) {
            String chunk = parts.get(i);
            String spellCheckResponseRaw = requestHtml(chunk);
            checkedSpells.addAll(spellCheckUtil.parseToFinding(spellCheckResponseRaw));
            throttle(i, parts.size());
        }

        return checkedSpells;
    }

    private String requestHtml(String chunk) {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("sentence", chunk);
        return spellCheckClient.post()
                .uri("/grammar_checker.do")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(String.class);
    }

    private void throttle(int index, int total) {

        if (index < total - 1) {
            try {
                Thread.sleep(DAUM_MIN_INTERVAL_MS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
