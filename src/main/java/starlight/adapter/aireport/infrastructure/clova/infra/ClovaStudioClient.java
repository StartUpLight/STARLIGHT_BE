package starlight.adapter.aireport.infrastructure.clova.infra;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import starlight.shared.dto.infrastructure.ClovaStudioResponse;
import starlight.adapter.aireport.infrastructure.clova.util.ClovaUtil;

import java.util.Map;

/**
 * Clova Studio API 호출 클라이언트.
 *
 * @deprecated 1.4.0 현재 사용 경로가 없으며, 필요 시 재도입할 수 있어 유지 중입니다.
 *             대체 구현체는 없습니다.
 */
@Component
@Deprecated(since = "1.4.0", forRemoval = false)
public class ClovaStudioClient {

    private final RestClient restClient;

    public ClovaStudioClient(@Qualifier("clovaStudioRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public ClovaStudioResponse check(String systemMsg, String userMsg, int criteriaSize) {
        Map<String, Object> body = ClovaUtil.buildClovaRequestBody(systemMsg, userMsg, criteriaSize);

        return restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(ClovaStudioResponse.class);
    }
}
