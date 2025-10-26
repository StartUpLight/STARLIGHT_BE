package starlight.adapter.ncp.ocr.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starlight.shared.dto.ClovaOcrResponse;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OcrResponseMerger 테스트")
class OcrResponseMergerUnitTest {

    @Test
    @DisplayName("null 입력 시 빈 응답 반환")
    void merge_ReturnsEmpty_WhenInputIsNull() {
        // when
        ClovaOcrResponse result = OcrResponseMerger.merge(null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.images()).isEmpty();
        assertThat(result.version()).isEqualTo("V2");
        assertThat(result.requestId()).isEqualTo("empty");
    }

    @Test
    @DisplayName("빈 리스트 입력 시 빈 응답 반환")
    void merge_ReturnsEmpty_WhenInputIsEmpty() {
        // when
        ClovaOcrResponse result = OcrResponseMerger.merge(List.of());

        // then
        assertThat(result).isNotNull();
        assertThat(result.images()).isEmpty();
    }

    @Test
    @DisplayName("단일 응답 병합")
    void merge_SingleResponse() {
        // given
        ClovaOcrResponse.ImageResult.Field field1 = new ClovaOcrResponse.ImageResult.Field(
                "text", "Hello", 0.95, "normal", false
        );
        ClovaOcrResponse.ImageResult image1 = new ClovaOcrResponse.ImageResult(
                "img1", "page1", "SUCCESS", null, List.of(field1)
        );
        ClovaOcrResponse response = ClovaOcrResponse.create("V2", "req1", List.of(image1));

        // when
        ClovaOcrResponse result = OcrResponseMerger.merge(List.of(response));

        // then
        assertThat(result.version()).isEqualTo("V2");
        assertThat(result.requestId()).isEqualTo("req1");
        assertThat(result.images()).hasSize(1);
        assertThat(result.images().get(0).fields()).hasSize(1);
        assertThat(result.images().get(0).fields().get(0).inferText()).isEqualTo("Hello");
    }

    @Test
    @DisplayName("다중 응답 병합 - 이미지 순서 유지")
    void merge_MultipleResponses_MaintainsOrder() {
        // given
        ClovaOcrResponse.ImageResult.Field field1 = new ClovaOcrResponse.ImageResult.Field(
                "text", "Page1", 0.95, "normal", false
        );
        ClovaOcrResponse.ImageResult.Field field2 = new ClovaOcrResponse.ImageResult.Field(
                "text", "Page2", 0.95, "normal", false
        );
        ClovaOcrResponse.ImageResult.Field field3 = new ClovaOcrResponse.ImageResult.Field(
                "text", "Page3", 0.95, "normal", false
        );

        ClovaOcrResponse.ImageResult image1 = new ClovaOcrResponse.ImageResult(
                "img1", "page1", "SUCCESS", null, List.of(field1)
        );
        ClovaOcrResponse.ImageResult image2 = new ClovaOcrResponse.ImageResult(
                "img2", "page2", "SUCCESS", null, List.of(field2)
        );
        ClovaOcrResponse.ImageResult image3 = new ClovaOcrResponse.ImageResult(
                "img3", "page3", "SUCCESS", null, List.of(field3)
        );

        ClovaOcrResponse response1 = ClovaOcrResponse.create("V2", "req1", List.of(image1));
        ClovaOcrResponse response2 = ClovaOcrResponse.create("V2", "req2", List.of(image2, image3));

        // when
        ClovaOcrResponse result = OcrResponseMerger.merge(List.of(response1, response2));

        // then
        assertThat(result.images()).hasSize(3);
        assertThat(result.images().get(0).fields().get(0).inferText()).isEqualTo("Page1");
        assertThat(result.images().get(1).fields().get(0).inferText()).isEqualTo("Page2");
        assertThat(result.images().get(2).fields().get(0).inferText()).isEqualTo("Page3");
    }

    @Test
    @DisplayName("첫 번째 응답의 version과 requestId 사용")
    void merge_UsesFirstResponseMetadata() {
        // given
        ClovaOcrResponse response1 = ClovaOcrResponse.create("V2", "first-request", List.of());
        ClovaOcrResponse response2 = ClovaOcrResponse.create("V3", "second-request", List.of());

        // when
        ClovaOcrResponse result = OcrResponseMerger.merge(List.of(response1, response2));

        // then
        assertThat(result.version()).isEqualTo("V2");
        assertThat(result.requestId()).isEqualTo("first-request");
    }

    @Test
    @DisplayName("images가 null인 응답 포함 시 안전하게 병합")
    void merge_HandlesNullImages() {
        // given
        ClovaOcrResponse.ImageResult.Field field1 = new ClovaOcrResponse.ImageResult.Field(
                "text", "Hello", 0.95, "normal", false
        );
        ClovaOcrResponse.ImageResult image1 = new ClovaOcrResponse.ImageResult(
                "img1", "page1", "SUCCESS", null, List.of(field1)
        );

        ClovaOcrResponse response1 = ClovaOcrResponse.create("V2", "req1", List.of(image1));
        ClovaOcrResponse response2 = new ClovaOcrResponse("V2", "req2", System.currentTimeMillis(), null);

        // when
        ClovaOcrResponse result = OcrResponseMerger.merge(List.of(response1, response2));

        // then
        assertThat(result.images()).hasSize(1);
        assertThat(result.images().get(0).fields().get(0).inferText()).isEqualTo("Hello");
    }

    @Test
    @DisplayName("10개 이상의 응답 병합")
    void merge_ManyResponses() {
        // given
        List<ClovaOcrResponse> responses = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            ClovaOcrResponse.ImageResult.Field field = new ClovaOcrResponse.ImageResult.Field(
                    "text", "Page" + i, 0.95, "normal", false
            );
            ClovaOcrResponse.ImageResult image = new ClovaOcrResponse.ImageResult(
                    "img" + i, "page" + i, "SUCCESS", null, List.of(field)
            );
            responses.add(ClovaOcrResponse.create("V2", "req" + i, List.of(image)));
        }

        // when
        ClovaOcrResponse result = OcrResponseMerger.merge(responses);

        // then
        assertThat(result.images()).hasSize(15);
        assertThat(result.images().get(0).fields().get(0).inferText()).isEqualTo("Page0");
        assertThat(result.images().get(14).fields().get(0).inferText()).isEqualTo("Page14");
    }

    @Test
    @DisplayName("빈 images를 가진 응답들 병합")
    void merge_EmptyImagesResponses() {
        // given
        ClovaOcrResponse response1 = ClovaOcrResponse.create("V2", "req1", List.of());
        ClovaOcrResponse response2 = ClovaOcrResponse.create("V2", "req2", List.of());

        // when
        ClovaOcrResponse result = OcrResponseMerger.merge(List.of(response1, response2));

        // then
        assertThat(result.images()).isEmpty();
    }
}