package starlight.adapter.spellcheck.webapi;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import starlight.application.spellcheck.required.SpellChecker;
import starlight.shared.apiPayload.response.ApiResponse;
import starlight.adapter.spellcheck.spellcheck.dto.Finding;
import starlight.adapter.spellcheck.spellcheck.DaumSpellChecker;
import starlight.adapter.spellcheck.webapi.dto.SpellCheckRequest;
import starlight.adapter.spellcheck.webapi.dto.SpellCheckResponse;
import starlight.adapter.spellcheck.webapi.swagger.SpellCheckApiDoc;

import java.util.List;

@RestController
@RequestMapping("/api/spell")
@RequiredArgsConstructor
public class SpellController implements SpellCheckApiDoc {

    private final SpellChecker spellChecker;

    @Override
    public ApiResponse<SpellCheckResponse> check(SpellCheckRequest spellCheckRequest) {
        String text = spellCheckRequest.text();

        List<Finding> typos = spellChecker.check(text);
        String corrected = spellChecker.applyTopSuggestions(text, typos);

        return ApiResponse.success(SpellCheckResponse.of(typos, corrected));
    }
}