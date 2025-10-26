package starlight.adapter.businessplan.webapi;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import starlight.adapter.businessplan.webapi.dto.SpellCheckRequest;
import starlight.adapter.businessplan.webapi.dto.SpellCheckResponse;
import starlight.adapter.businessplan.spellcheck.dto.Finding;
import starlight.adapter.businessplan.webapi.swagger.SpellCheckApiDoc;
import starlight.application.businessplan.required.SpellChecker;
import starlight.shared.apiPayload.response.ApiResponse;

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