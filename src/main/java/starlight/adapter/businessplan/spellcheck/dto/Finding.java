package starlight.adapter.businessplan.spellcheck.dto;

import java.util.List;

public record Finding(
        String type,              // data-error-type (space, spell, doubt, ...)
        String severity,          // txt_word 강조: error / doubt / normal
        String token,             // 잘못된 원문 토큰(data-error-input)
        List<String> suggestions, // 교정 제안(data-error-output) - 보통 1개
        String visible,           // 화면에 보이는 제안 텍스트(.txt_word)
        String original,          // 원문 스팬(.inner_spell)
        String context,           // 주변 문맥(data-error-context)
        String help,              // 도움말(ul#help 등)
        List<String> examples     // 예문 리스트(div.lst ul#examples li)
) {
    public static Finding of(String type, String severity, String token, List<String> suggestions,
                             String visible, String original, String context, String help, List<String> examples)
    {
        String severe = (severity == null || severity.isBlank()) ? "normal" : severity;
        List<String> suggestion = (suggestions == null) ? List.of() : List.copyOf(suggestions);
        List<String> example   = (examples == null)    ? List.of() : List.copyOf(examples);
        return new Finding(
                nullIfBlank(type),
                severe,
                nullIfBlank(token),
                suggestion,
                nullIfBlank(visible),
                nullIfBlank(original),
                nullIfBlank(context),
                nullIfBlank(help),
                example
        );
    }

    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}