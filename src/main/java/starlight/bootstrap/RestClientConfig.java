package starlight.bootstrap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.UUID;

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

    @Bean(name = "clovaOcrRestClient")
    public RestClient clovaOcrRestClient(
            @Value("${cloud.ncp.ocr.endpoint}") String endpoint,
            @Value("${cloud.ncp.ocr.secret}") String secret
    ) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(120));

        return RestClient.builder()
                .baseUrl(endpoint)
                .requestFactory(factory)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("X-OCR-SECRET", secret)
                .build();
    }

    /**
     * (옵션) 외부 PDF를 다운로드할 때 쓰는 경량 클라이언트
     * - 일반적인 타임아웃
     * - UA만 지정 (일부 서버 호환)
     * 필요 없으면 이 빈은 제거해도 됨.
     */
    @Bean(name = "pdfDownloadRestClient")
    public RestClient pdfDownloadRestClient() {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(60));

        return RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("User-Agent", "Mozilla/5.0")
                .build();
    }

    @Bean(name = "clovaStudioRestClient")
    public RestClient clovaStudioRestClient(
            @Value("${cloud.ncp.studio.host}") String clovaHost,
            @Value("${cloud.ncp.studio.api-key}") String apiKey,
            @Value("${cloud.ncp.studio.model}") String model
    ) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(60));

        return RestClient.builder()
                .baseUrl(String.format("%s/%s", clovaHost, model))
                .requestFactory(factory)
                .defaultRequest(request -> {
                    request.header("X-NCP-CLOVASTUDIO-REQUEST-ID", UUID.randomUUID().toString());
                })
                .defaultHeader("Authorization", "Bearer " + apiKey) // Bearer only
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean(name = "tossRestClient")
    public RestClient tossRestClient(
            @Value("${toss.secretKey}") String secretKey,
            @Value("${toss.baseUrl}") String baseUrl
    ) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(20)); // 필요 시 조정

        String basic = java.util.Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(java.nio.charset.StandardCharsets.UTF_8));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader("Authorization", "Basic " + basic)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
