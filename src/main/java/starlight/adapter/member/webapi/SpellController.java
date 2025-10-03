package starlight.adapter.member.webapi;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import starlight.application.member.required.SpellChecker;
import starlight.shared.apiPayload.response.ApiResponse;
import starlight.adapter.member.spellcheck.dto.Finding;
import starlight.adapter.member.webapi.dto.SpellCheckRequest;
import starlight.adapter.member.webapi.dto.SpellCheckResponse;
import starlight.adapter.member.webapi.swagger.SpellCheckApiDoc;

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