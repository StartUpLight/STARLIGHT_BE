package starlight.bootstrap;

import org.springframework.beans.factory.annotation.Value;
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

    @Bean(name = "clovaOcrRestClient")
    public RestClient clovaOcrRestClient(
            @Value("${cloud.ncp.ocr.endpoint}") String endpoint,
            @Value("${cloud.ncp.ocr.secret}") String secretRaw
    ) {
        String secret = secretRaw == null ? "" : secretRaw.trim(); // 공백 제거
        if (secret.isEmpty()) throw new IllegalStateException("Missing 'clova.ocr.secret'");

        var factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(120));     // ↑ OCR 대기시간 확보

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
    @Bean(name = "downloadClient")
    public RestClient downloadClient() {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(60));

        return RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("User-Agent", "STARLIGHT-OCR/1.0")
                .build();
    }
}