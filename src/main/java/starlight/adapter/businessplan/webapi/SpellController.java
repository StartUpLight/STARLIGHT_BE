package starlight.adapter.businessplan.webapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import starlight.adapter.businessplan.webapi.dto.SpellCheckResponse;
import starlight.adapter.businessplan.spellcheck.dto.Finding;
import starlight.adapter.businessplan.webapi.swagger.SpellCheckApiDoc;
import starlight.application.businessplan.required.SpellCheckerPort;
import starlight.adapter.businessplan.webapi.dto.SubSectionCreateRequest;
import starlight.application.businessplan.util.PlainTextExtractUtils;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/v1/business-plans")
@RequiredArgsConstructor
public class SpellController implements SpellCheckApiDoc {

    private final ObjectMapper objectMapper;
    private final SpellCheckerPort spellChecker;

    @Override
    public ApiResponse<SpellCheckResponse> check(
            @Valid @RequestBody SubSectionCreateRequest subSectionCreateRequest
    ) {
        String text = PlainTextExtractUtils.extractPlainText(objectMapper, subSectionCreateRequest);

        List<Finding> typos = spellChecker.check(text);
        String corrected = spellChecker.applyTopSuggestions(text, typos);

        return ApiResponse.success(SpellCheckResponse.of(typos, corrected));
    }
}