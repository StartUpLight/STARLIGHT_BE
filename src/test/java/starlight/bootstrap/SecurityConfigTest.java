package starlight.bootstrap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import starlight.adapter.member.auth.security.filter.ExceptionFilter;
import starlight.adapter.member.auth.security.filter.JwtFilter;
import starlight.adapter.member.auth.security.handler.JwtAccessDeniedHandler;
import starlight.adapter.member.auth.security.handler.JwtAuthenticationHandler;
import starlight.adapter.member.auth.security.oauth2.CustomOAuth2UserService;
import starlight.adapter.member.auth.security.oauth2.OAuth2SuccessHandler;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private JwtFilter jwtFilter;
    @MockitoBean private ExceptionFilter exceptionFilter;
    @MockitoBean private JwtAccessDeniedHandler jwtAccessDeniedHandler;
    @MockitoBean private JwtAuthenticationHandler jwtAuthenticationEntryPoint;
    @MockitoBean private CustomOAuth2UserService oAuth2UserService;
    @MockitoBean private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @WithMockUser
    void oauth2Login_실패_핸들러_401_반환() throws Exception {
        mockMvc.perform(get("/login/oauth2/code/kakao")
                        .param("error", "invalid_token"))
                .andExpect(status().isUnauthorized());
    }
}
