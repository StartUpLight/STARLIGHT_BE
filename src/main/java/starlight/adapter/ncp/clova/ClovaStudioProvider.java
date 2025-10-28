package starlight.adapter.ncp.clova;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import starlight.adapter.ncp.clova.infra.ClovaStudioClient;
import starlight.adapter.ncp.clova.util.ClovaUtil;
import starlight.application.infrastructure.provided.CheckListGrader;
import starlight.application.prompt.required.PromptFinder;
import starlight.shared.dto.infrastructure.ClovaStudioResponse;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClovaStudioProvider implements CheckListGrader {

    private final ClovaStudioClient clovaStudioClient;
    private final PromptFinder promptFinder;

    @Override
    public List<Boolean> check(String sectionName, String userMsg, int criteriaSize){
        String systemPrompt = "너는 사업계획서 전문가야";
        List<String> criteria = promptFinder.getSectionCriteria(sectionName);
        String userPrompt = ClovaUtil.buildUserContent(userMsg, criteria);

        ClovaStudioResponse response = clovaStudioClient.check(systemPrompt, userPrompt, criteriaSize);

        return ClovaUtil.toBooleanList(response.result().message().content(), criteriaSize);
    }
}
