package starlight.adapter.ncp.clova.infra;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import starlight.shared.dto.ClovaResponse;
import starlight.adapter.ncp.clova.util.ClovaUtil;
import starlight.application.infrastructure.provided.CheckListGrader;

import java.util.Map;

@Component
public class ClovaStudioClient {

    private final RestClient restClient;

    public ClovaStudioClient(@Qualifier("clovaClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public ClovaResponse check(String systemMsg, String userMsg, int criteriaSize) {
        Map<String, Object> body = ClovaUtil.buildClovaRequestBody(systemMsg, userMsg, criteriaSize);

        return restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(ClovaResponse.class);
    }
}