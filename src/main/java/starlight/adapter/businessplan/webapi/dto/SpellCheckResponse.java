package starlight.adapter.businessplan.webapi.dto;

import starlight.adapter.businessplan.spellcheck.dto.Finding;

import java.util.List;

public record SpellCheckResponse(

        List<Finding> typos,

        String corrected
) {
    public static SpellCheckResponse of(List<Finding> typos, String corrected) {
        return new SpellCheckResponse(typos, corrected);
    }
}