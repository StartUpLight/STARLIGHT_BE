package starlight.adapter.auth.security.oauth2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(OAuth2LoginFlowTest.TestBeans.class)
class OAuth2LoginFlowTest {

    @Autowired MockMvc mvc;
    @Autowired TestOAuth2Objects testOAuth2Objects;

    @Test
    void protected_endpoint_after_oauth2Login() throws Exception {
        DefaultOAuth2User user = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("response", Map.of(
                        "id", "nid-42",
                        "email", "u@naver.com",
                        "name", "홍길동",
                        "profile_image", "http://img"
                )),
                "response"
        );

        ClientRegistration reg = testOAuth2Objects.naverRegistration();

        mvc.perform(get("/api/protected")
                        .with(oauth2Login()
                                .oauth2User(user)
                                .clientRegistration(reg)
                        )
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationId").value("naver"))
                .andExpect(jsonPath("$.principalName").exists());
    }

    @TestConfiguration
    static class TestBeans {
        @Bean
        TestOAuth2Objects testOAuth2Objects() {
            return new TestOAuth2Objects();
        }

        // 테스트용 컨트롤러: 인증 성공 시 registrationId와 principal 이름을 반환
        @RestController
        static class ProtectedEchoController {
            @GetMapping("/api/protected")
            public Map<String, String> me(org.springframework.security.core.Authentication authentication) {
                org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken token =
                        (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication;
                String regId = token.getAuthorizedClientRegistrationId();
                String name = token.getName(); // DefaultOAuth2User의 getName() (여기선 "response")
                return Map.of("registrationId", regId, "principalName", name);
            }
        }
    }
}

