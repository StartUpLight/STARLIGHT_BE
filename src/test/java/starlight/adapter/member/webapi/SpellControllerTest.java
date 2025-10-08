package starlight.adapter.member.webapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import starlight.adapter.member.spellcheck.dto.Finding;
import starlight.application.member.required.SpellChecker;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
class SpellControllerTest {

    @Autowired
    MockMvc mvc;

    @TestConfiguration
    static class TestBeans {

        @Bean
        SpellChecker spellChecker() {
            return new SpellChecker() {

                @Override
                public List<Finding> check(String sentence) {
                    if (sentence.contains("teh")) {
                        return List.of(new Finding(
                                "spell",
                                "error",
                                "teh",
                                List.of("the"),
                                "맞춤법 오류",
                                "teh",
                                "teh cat",
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
        mvc.perform(post("/api/spell/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"teh cat\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.corrected").value("the cat"))
                .andExpect(jsonPath("$.data.typos").isArray());
    }
}
