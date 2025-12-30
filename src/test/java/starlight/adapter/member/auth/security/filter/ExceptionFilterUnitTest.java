package starlight.adapter.member.auth.security.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import starlight.shared.apiPayload.exception.GlobalErrorType;
import starlight.shared.apiPayload.exception.GlobalException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ExceptionFilterUnitTest {

    private final ObjectMapper om = new ObjectMapper();
    private final ExceptionFilter filter = new ExceptionFilter(om);

    @Test
    void wraps_GlobalException_to_Json_and_sets_status_400() throws Exception {
        // given
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/any");
        MockHttpServletResponse res = new MockHttpServletResponse();

        FilterChain chain = (request, response) -> {
            throw new GlobalException(GlobalErrorType.BAD_REQUEST); // 400 기대
        };

        // when
        filter.doFilter(req, res, chain);

        // then
        assertThat(res.getStatus()).isEqualTo(400);
        assertThat(res.getContentType()).isEqualTo("application/json;charset=UTF-8");

        JsonNode body = om.readTree(res.getContentAsString());
        assertThat(body.path("result").asText()).isEqualTo("ERROR");
        assertThat(body.path("data").isNull()).isTrue();
        assertThat(body.at("/error/code").asText()).isEqualTo("BAD_REQUEST");
        assertThat(body.at("/error/message").asText()).contains("잘못된 요청");
    }

    @Test
    void passes_through_when_no_exception() throws Exception {
        // given
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/ok");
        MockHttpServletResponse res = new MockHttpServletResponse();

        FilterChain chain = (request, response) -> {
            // 정상 흐름: 예외 없음
            response.getWriter().write("OK");
        };

        // when
        filter.doFilter(req, res, chain);

        // then
        assertThat(res.getStatus()).isEqualTo(200);
        assertThat(res.getContentType()).isNull(); // 체인이 안 정하면 null일 수 있음
        assertThat(res.getContentAsString()).isEqualTo("OK");
    }

    @Test
    void does_nothing_if_response_already_committed() throws Exception {
        // given
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/committed");
        MockHttpServletResponse res = new MockHttpServletResponse();

        FilterChain chain = (request, response) -> {
            response.getWriter().write("partial");
            response.flushBuffer(); // 응답 커밋
            throw new GlobalException(GlobalErrorType.BAD_REQUEST); // 이후 필터는 건드리면 안 됨
        };

        // when
        filter.doFilter(req, res, chain);

        // then
        assertThat(res.isCommitted()).isTrue();
        assertThat(res.getStatus()).isEqualTo(200);
        assertThat(res.getContentAsString()).isEqualTo("partial");
    }
}
