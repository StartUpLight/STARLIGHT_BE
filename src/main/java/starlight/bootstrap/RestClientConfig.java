package starlight.bootstrap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient spellCheckClient() {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(12));

        return RestClient.builder()
                .baseUrl("https://dic.daum.net")
                .requestFactory(factory)
                .defaultHeader("User-Agent", "Mozilla/5.0")
                .defaultHeader("Accept-Language", "ko,en;q=0.9")
                .build();
    }
}