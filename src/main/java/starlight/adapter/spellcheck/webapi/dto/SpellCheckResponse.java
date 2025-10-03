package starlight.adapter.spellcheck.webapi.dto;

import starlight.adapter.spellcheck.spellcheck.dto.Finding;

import java.util.List;

public record SpellCheckResponse(
        List<Finding> typos,
        String corrected
) {
    public static SpellCheckResponse of(List<Finding> typos, String corrected) {
        return new SpellCheckResponse(typos, corrected);
    }
}