package starlight.application.member.required;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import starlight.adapter.member.spellcheck.DaumSpellChecker;
import starlight.adapter.member.spellcheck.dto.Finding;
import starlight.adapter.member.spellcheck.util.SpellCheckUtil;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(components = DaumSpellChecker.class)
@Import(DaumSpellCheckerHttpTest.TestConfig.class)
class DaumSpellCheckerHttpTest {

    @Autowired DaumSpellChecker daumSpellChecker;
    @Autowired MockRestServiceServer server;

    // JPA가 올라오지 못하도록
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @TestConfiguration
    static class TestConfig {

        @Bean SpellCheckUtil spellCheckUtil() { return new SpellCheckUtil(); }

        @Bean RestClient spellCheckClient(RestClient.Builder builder) {
            return builder
                    .baseUrl("http://localhost")
                    .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0")
                    .defaultHeader(HttpHeaders.ACCEPT_LANGUAGE, "ko,en;q=0.9")
                    .build();
        }
    }

    @Test
    @DisplayName("check - 유효한 응답 파싱")
    void check_parsesValidResponse() {

        //given
        String html = """
            <html>
            <head></head>
            <body>
              <div id="mArticle">
                <a class="txt_spell" data-error-type="spell"
                   data-error-input="안뇽하세요"
                   data-error-output="안녕하세요"
                   data-error-context="안뇽하세요">
                  <span class="txt_word txt_error">안뇽하세요</span>
                  <span class="inner_spell">안뇽하세요</span>
                  <span name="contents">
                    <ul id="help"><li>맞춤법을 확인하세요</li></ul>
                    <div class="lst">
                      <ul id="examples">
                        <li>예: 안녕하세요</li>
                      </ul>
                    </div>
                  </span>
                </a>
              </div>
            </body>
            </html>
        """;

        server.expect(once(), requestTo("http://localhost/grammar_checker.do"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andRespond(withSuccess(html, MediaType.TEXT_HTML));

        // when
        List<Finding> findings = daumSpellChecker.check("안뇽하세요");

        // then
        assertThat(findings).hasSize(1);
        Finding f = findings.get(0);
        assertThat(f.token()).isEqualTo("안뇽하세요");
        assertThat(f.suggestions()).contains("안녕하세요");
        assertThat(f.severity()).isEqualTo("error");

        server.verify();
    }

    @Test
    @DisplayName("check - 429 Too Many Requests 처리")
    void check_handlesRateLimitOrError() {

        //given
        server.expect(once(), requestTo("http://localhost/grammar_checker.do"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        // when // then
        assertThatThrownBy(() -> daumSpellChecker.check("안뇽하세요"))
                .isInstanceOf(RestClientException.class);
    }
}
