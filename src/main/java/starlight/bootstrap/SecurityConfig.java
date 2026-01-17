package starlight.bootstrap;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import starlight.adapter.member.auth.security.filter.ExceptionFilter;
import starlight.adapter.member.auth.security.filter.JwtFilter;
import starlight.adapter.member.auth.security.handler.JwtAccessDeniedHandler;
import starlight.adapter.member.auth.security.handler.JwtAuthenticationHandler;
import starlight.adapter.member.auth.security.oauth2.CustomOAuth2UserService;
import starlight.adapter.member.auth.security.oauth2.OAuth2SuccessHandler;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${cors.origin.server}") String ServerBaseUrl;
    @Value("${cors.origin.client}") String clientBaseUrl;
    @Value("${cors.origin.office}") String officeBaseUrl;
    @Value("${cors.origin.develop}") String devBaseUrl;
    @Value("${backoffice.auth.username}") String backofficeUsername;
    @Value("${backoffice.auth.password}") String backofficePassword;

    private final Environment environment;
    private final JwtFilter jwtFilter;
    private final ExceptionFilter exceptionFilter;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationHandler jwtAuthenticationEntryPoint;
    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    @Order(1)
    public SecurityFilterChain backofficeFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler csrfTokenRequestHandler = new CsrfTokenRequestAttributeHandler();
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        boolean isDevProfile = List.of(environment.getActiveProfiles()).contains("dev");
        if (!isDevProfile) {
            csrfTokenRepository.setCookieCustomizer(cookie -> cookie
                    .domain("starlight-official.co.kr")
                    .sameSite("None")
                    .secure(true)
            );
        }

        http.securityMatcher("/v1/backoffice/mail/**", "/login", "/logout")
                .cors(Customizer.withDefaults())
                .csrf((csrf) -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(csrfTokenRequestHandler)
                        .ignoringRequestMatchers("/login", "/logout")
                )
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .requestMatchers("/login", "/logout").permitAll()
                                .anyRequest().hasRole("BACKOFFICE")
                )
                .formLogin(Customizer.withDefaults())
                .logout(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);

        http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        http.exceptionHandling((exceptionHandling) ->
                exceptionHandling
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        );
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(exceptionFilter, JwtFilter.class);

        http.authorizeHttpRequests((authorize) ->
                authorize
                        .requestMatchers("/error/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/index.html", "/ops.html", "/payment.html", "/api/payment/**").permitAll()
                        .requestMatchers("/v1/auth/**","/v1/user/**", "/v1/experts", "/v1/experts/*").permitAll()
                        .requestMatchers("/login/**", "/oauth2/**", "/login/oauth2/**", "/public/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/v1/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**").permitAll()

                        // Expert Application
                        .requestMatchers(HttpMethod.GET,  "/v1/expert-reports/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/expert-reports/*").permitAll()

                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth -> oauth
                        .loginPage("/login/oauth2/code/kakao")
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            log.warn("OAuth2 login failed: {}", exception.getMessage(), exception);
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                clientBaseUrl,
                ServerBaseUrl,
                devBaseUrl,
                officeBaseUrl
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.builder()
                .username(backofficeUsername)
                .password(passwordEncoder.encode(backofficePassword))
                .roles("BACKOFFICE")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new HttpStatusReturningLogoutSuccessHandler();
    }
}
