package starlight.adapter.member.auth.webapi;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import starlight.adapter.member.auth.security.auth.AuthDetails;
import starlight.adapter.member.auth.security.auth.AuthDetailsService;
import starlight.adapter.member.auth.security.filter.JwtFilter;
import starlight.application.member.auth.provided.AuthUseCase;
import starlight.application.member.auth.provided.dto.AuthMemberResult;
import starlight.application.member.auth.provided.dto.AuthTokenResult;
import starlight.application.member.auth.provided.dto.SignUpCommand;
import starlight.bootstrap.SecurityConfig;
import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;
import starlight.domain.member.enumerate.MemberType;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        JwtFilter.class,
                        SecurityConfig.class
                })
        },
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {"jwt.header=Authorization"})
@Import(AuthControllerSliceTest.AuthTestConfig.class) // 커스텀 argument resolver 등록
class AuthControllerSliceTest {

    @Autowired MockMvc mvc;

    @MockitoBean AuthUseCase authUseCase;
    @MockitoBean AuthTokenResolver tokenResolver;
    @MockitoBean AuthDetailsService authDetailsService;

    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @TestConfiguration
    static class AuthTestConfig implements WebMvcConfigurer {
        @Bean
        HandlerMethodArgumentResolver authDetailsResolver() {
            return new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                    return parameter.getParameterType().equals(AuthDetails.class);
                }
                @Override
                public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                              org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                                              org.springframework.web.context.request.NativeWebRequest webRequest,
                                              org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
                    AuthDetails authDetails = Mockito.mock(AuthDetails.class);
                    Member member = Member.create("tester","tester@ex.com", null, MemberType.FOUNDER, null, "image.png");
                    when(authDetails.getUser()).thenReturn(member);
                    return authDetails;
                }
            };
        }

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(authDetailsResolver());
        }
    }

    @Test
    void signOut_OK_토큰파싱_후_서비스호출() throws Exception {
        when(tokenResolver.resolveRefreshToken(any())).thenReturn("RT");
        when(tokenResolver.resolveAccessToken(any())).thenReturn("AT");

        mvc.perform(post("/v1/auth/sign-out"))
                .andExpect(status().isOk());

        verify(authUseCase).signOut("RT", "AT");
    }

    @Test
    void recreate_OK_헤더에서_토큰읽어_서비스호출() throws Exception {
        when(tokenResolver.resolveRefreshToken(any())).thenReturn("REAL_RT");
        when(authUseCase.reissue(eq("REAL_RT"), any(Member.class)))
                .thenReturn(new AuthTokenResult("NEW_AT", "RT_OR_NEW"));

        mvc.perform(get("/v1/auth/recreate"))
                .andExpect(status().isOk());

        verify(authUseCase).reissue(eq("REAL_RT"), any(Member.class));
    }

    @Test
    void signIn_OK() throws Exception {
        when(authUseCase.signIn(argThat(req ->
                "a@b.com".equals(req.email()) && "pw".equals(req.password())
        ))).thenReturn(new AuthTokenResult("AT", "RT"));

        mvc.perform(post("/v1/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.com\",\"password\":\"pw\"}"))
                .andExpect(status().isOk());

        verify(authUseCase).signIn(argThat(req ->
                "a@b.com".equals(req.email()) && "pw".equals(req.password())
        ));
    }

    @Test
    void signUp_OK() throws Exception {
        when(authUseCase.signUp(any(SignUpCommand.class))).thenAnswer(invocation -> {
            SignUpCommand command = invocation.getArgument(0);
            Credential credential = Credential.create("hashedPassword");
            Member member = Member.create(
                    command.name(),
                    command.email(),
                    command.phoneNumber(),
                    MemberType.FOUNDER,
                    credential,
                    "image.png"
            );
            return AuthMemberResult.from(member);
        });

        mvc.perform(post("/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"정성호\",\"email\":\"user@ex.com\",\"password\":\"pw\",\"phoneNumber\":\"010-1234-5678\"}"))
                .andExpect(status().isOk());

        verify(authUseCase).signUp(argThat(req ->
                "user@ex.com".equals(req.email()) && "pw".equals(req.password()) && "010-1234-5678".equals(req.phoneNumber())
        ));
    }
}
