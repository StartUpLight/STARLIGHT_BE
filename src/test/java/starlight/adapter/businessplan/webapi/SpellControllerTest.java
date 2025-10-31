package starlight.adapter.businessplan.webapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import starlight.adapter.businessplan.spellcheck.dto.Finding;
import starlight.application.businessplan.required.SpellChecker;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
class SpellControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @TestConfiguration
    static class TestBeans {
        @Bean
        SpellChecker spellChecker() {
            return new SpellChecker() {
                @Override
                public List<Finding> check(String sentence) {
                    if (sentence.contains("teh")) {
                        return List.of(new Finding(
                                "spell", "error", "teh",
                                List.of("the"),
                                "맞춤법 오류", "teh", "teh cat",
                                "맞춤법을 확인하세요",
                                List.of()
                        ));
                    }
                    return List.of();
                }
                @Override
                public String applyTopSuggestions(String original, List<Finding> findings) {
                    return original.replace("teh", "the");
                }
            };
        }
    }

    @Test
    @DisplayName("End-to-End - 가짜 맞춤법 검사기로 통합 테스트")
    void endToEnd_withFakeSpellChecker() throws Exception {
        var body = Map.of(
                "sectionName", "OVERVIEW",
                "checks", List.of(true, false, true), // 3~10개 사이
                "meta", Map.of(
                        "author", "tester",
                        "createdAt", "2025-10-28"
                ),
                "blocks", List.of(
                        Map.of(
                                "meta", Map.of("title", "Intro"),
                                "content", List.of(
                                        Map.of("type", "text", "value", "teh cat")
                                )
                        )
                )
        );

        mvc.perform(post("/v1/business-plans/spellcheck")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.corrected").value("the cat"))
                .andExpect(jsonPath("$.data.typos").isArray())
                .andExpect(jsonPath("$.data.typos[0].token").value("teh"));
    }
}
