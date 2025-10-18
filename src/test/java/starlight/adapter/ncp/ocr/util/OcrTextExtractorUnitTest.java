package starlight.adapter.ncp.ocr.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starlight.shared.dto.ClovaOcrResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OcrTextExtractor 테스트")
class OcrTextExtractorUnitTest {

    @Test
    @DisplayName("null 입력 시 빈 문자열 반환")
    void toPlainText_ReturnsEmpty_WhenInputIsNull() {
        // when
        String result = OcrTextExtractor.toPlainText(null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("images가 null인 응답 처리")
    void toPlainText_HandlesNullImages() {
        // given
        ClovaOcrResponse response = new ClovaOcrResponse("V2", "req1", System.currentTimeMillis(), null);

        // when
        String result = OcrTextExtractor.toPlainText(response);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("단일 페이지 텍스트 추출")
    void toPlainText_SinglePage() {
        // given
        ClovaOcrResponse.ImageResult.Field field1 = new ClovaOcrResponse.ImageResult.Field(
                "text", "Hello", 0.95, "normal", false
        );
        ClovaOcrResponse.ImageResult.Field field2 = new ClovaOcrResponse.ImageResult.Field(
                "text", "World", 0.95, "normal", true
        );
        ClovaOcrResponse.ImageResult image = new ClovaOcrResponse.ImageResult(
                "img1", "page1", "SUCCESS", null, List.of(field1, field2)
        );
        ClovaOcrResponse response = ClovaOcrResponse.create("V2", "req1", List.of(image));

        // when
        String result = OcrTextExtractor.toPlainText(response);

        // then
        assertThat(result).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("다중 페이지 텍스트 추출 - 구분선 포함")
    void toPlainText_MultiplePages_WithSeparator() {
        // given
        ClovaOcrResponse.ImageResult.Field field1 = new ClovaOcrResponse.ImageResult.Field(
                "text", "Page1", 0.95, "normal", true
        );
        ClovaOcrResponse.ImageResult.Field field2 = new ClovaOcrResponse.ImageResult.Field(
                "text", "Page2", 0.95, "normal", true
        );
        ClovaOcrResponse.ImageResult image1 = new ClovaOcrResponse.ImageResult(
                "img1", "page1", "SUCCESS", null, List.of(field1)
        );
        ClovaOcrResponse.ImageResult image2 = new ClovaOcrResponse.ImageResult(
                "img2", "page2", "SUCCESS", null, List.of(field2)
        );
        ClovaOcrResponse response = ClovaOcrResponse.create("V2", "req1", List.of(image1, image2));

        // when
        String result = OcrTextExtractor.toPlainText(response);

        // then
        assertThat(result).isEqualTo("Page1\n\n-----\n\nPage2");
    }

    @Test
    @DisplayName("신뢰도 낮은 필드는 제외")
    void toPlainText_FiltersLowConfidence() {
        // given
        ClovaOcrResponse.ImageResult.Field highConfidence = new ClovaOcrResponse.ImageResult.Field(
                "text", "Good", 0.95, "normal", false
        );
        ClovaOcrResponse.ImageResult.Field lowConfidence = new ClovaOcrResponse.ImageResult.Field(
                "text", "Bad", 0.70, "normal", false
        );
        ClovaOcrResponse.ImageResult.Field anotherHigh = new ClovaOcrResponse.ImageResult.Field(
                "text", "Text", 0.90, "normal", true
        );
        ClovaOcrResponse.ImageResult image = new ClovaOcrResponse.ImageResult(
                "img1", "page1", "SUCCESS", null, List.of(highConfidence, lowConfidence, anotherHigh)
        );
        ClovaOcrResponse response = ClovaOcrResponse.create("V2", "req1", List.of(image));

        // when
        String result = OcrTextExtractor.toPlainText(response);

        // then
        assertThat(result).isEqualTo("Good Text");
        assertThat(result).doesNotContain("Bad");
    }

    @Test
    @DisplayName("신뢰도 경계값 테스트 - 0.85")
    void toPlainText_ConfidenceThreshold() {
        // given
        ClovaOcrResponse.ImageResult.Field exactThreshold = new ClovaOcrResponse.ImageResult.Field(
                "text", "Exact", 0.85, "normal", false
        );
        ClovaOcrResponse.ImageResult.Field justBelow = new ClovaOcrResponse.ImageResult.Field(
                "text", "Below", 0.849, "normal", false
        );
        ClovaOcrResponse.ImageResult.Field justAbove = new ClovaOcrResponse.ImageResult.Field(
                "text", "Above", 0.851, "normal", true
        );
        ClovaOcrResponse.ImageResult image = new ClovaOcrResponse.ImageResult(
                "img1", "page1", "SUCCESS", null, List.of(exactThreshold, justBelow, justAbove)
        );
        ClovaOcrResponse response = ClovaOcrResponse.create("V2", "req1", List.of(image));

        // when
        String result = OcrTextExtractor.toPlainText(response);

        // then
        assertThat(result).contains("Exact");
        assertThat(result).contains("Above");
        assertThat(result).doesNotContain("Below");
    }

    @Test
    @DisplayName("줄바꿈 처리")
    void toPlainText_HandlesLineBreaks() {
        // given
        ClovaOcrResponse.ImageResult.Field field1 = new ClovaOcrResponse.ImageResult.Field(
                "text", "Line1", 0.95, "normal", true
        );
        ClovaOcrResponse.ImageResult.Field field2 = new ClovaOcrResponse.ImageResult.Field(
                "text", "Line2", 0.95, "normal", true
        );
        ClovaOcrResponse.ImageResult.Field field3 = new ClovaOcrResponse.ImageResult.Field(
                "text", "Line3", 0.95, "normal", false
        );
        ClovaOcrResponse.ImageResult image = new ClovaOcrResponse.ImageResult(
                "img1", "page1", "SUCCESS", null, List.of(field1, field2, field3)
        );
        ClovaOcrResponse response = ClovaOcrResponse.create("V2", "req1", List.of(image));

        // when
        String result = OcrTextExtractor.toPlainText(response);

        // then
        assertThat(result).isEqualTo("Line1\nLine2\nLine3");
    }

    @Test
    @DisplayName("공백 정규화 - 연속 공백 제거")
    void toPlainText_NormalizesWhitespace() {
        // given
        ClovaOcrResponse.ImageResult.Field field = new ClovaOcrResponse.ImageResult.Field(
                "text", "Hello    World", 0.95, "normal", true
        );
        ClovaOcrResponse.ImageResult image = new ClovaOcrResponse.ImageResult(
                "img1", "page1", "SUCCESS", null, List.of(field)
        );
        ClovaOcrResponse response = ClovaOcrResponse.create("V2", "req1", List.of(image));

        // when
        String result = OcrTextExtractor.toPlainText(response);

        // then
        assertThat(result).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("구두점 앞 공백 제거")
    void toPlainText_RemovesSpaceBeforePunctuation() {
        // given
        ClovaOcrResponse.ImageResult.Field field = new ClovaOcrResponse.ImageResult.Field(
                "text", "Hello , World !", 0.95, "normal", true
        );
        ClovaOcrResponse.ImageResult image = new ClovaOcrResponse.ImageResult(
                "img1", "page1", "SUCCESS", null, List.of(field)
        );
        ClovaOcrResponse response = ClovaOcrResponse.create("V2", "req1", List.of(image));

        // when
        String result = OcrTextExtractor.toPlainText(response);

        // then
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("괄호 주변 공백 정리")
    void toPlainText_NormalizesParentheses() {
        // given
        ClovaOcrResponse.ImageResult.Field field = new ClovaOcrResponse.ImageResult.Field(
                "text", "Hello ( World )", 0.95, "normal", true
        );
        ClovaOcrResponse.ImageResult image = new ClovaOcrResponse.ImageResult(
                "img1", "page1", "SUCCESS", null, List.of(field)
        );
        ClovaOcrResponse response = ClovaOcrResponse.create("V2", "req1", List.of(image));

        // when
        String result = OcrTextExtractor.toPlainText(response);

        // then
        assertThat(result).isEqualTo("Hello (World)");
    }

    @Test
    @DisplayName("inferText가 null인 필드 처리")
    void toPlainText_HandlesNullInferText() {
        // given
        ClovaOcrResponse.ImageResult.Field field1 = new ClovaOcrResponse.ImageResult.Field(
                "text", "Hello", 0.95, "normal", false
        );
        ClovaOcrResponse.ImageResult.Field field2 = new ClovaOcrResponse.ImageResult.Field(
                "text", null, 0.95, "normal", false
        );
        ClovaOcrResponse.ImageResult.Field field3 = new ClovaOcrResponse.ImageResult.Field(
                "text", "World", 0.95, "normal", true
        );
        ClovaOcrResponse.ImageResult image = new ClovaOcrResponse.ImageResult(
                "img1", "page1", "SUCCESS", null, List.of(field1, field2, field3)
        );
        ClovaOcrResponse response = ClovaOcrResponse.create("V2", "req1", List.of(image));

        // when
        String result = OcrTextExtractor.toPlainText(response);

        // then
        assertThat(result).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("confidence가 null인 필드는 제외")
    void toPlainText_SkipsNullConfidence() {
        // given
        ClovaOcrResponse.ImageResult.Field field1 = new ClovaOcrResponse.ImageResult.Field(
                "text", "Hello", 0.95, "normal", false
        );
        ClovaOcrResponse.ImageResult.Field field2 = new ClovaOcrResponse.ImageResult.Field(
                "text", "Skip", null, "normal", false
        );
        ClovaOcrResponse.ImageResult.Field field3 = new ClovaOcrResponse.ImageResult.Field(
                "text", "World", 0.95, "normal", true
        );
        ClovaOcrResponse.ImageResult image = new ClovaOcrResponse.ImageResult(
                "img1", "page1", "SUCCESS", null, List.of(field1, field2, field3)
        );
        ClovaOcrResponse response = ClovaOcrResponse.create("V2", "req1", List.of(image));

        // when
        String result = OcrTextExtractor.toPlainText(response);

        // then
        assertThat(result).isEqualTo("Hello World");
        assertThat(result).doesNotContain("Skip");
    }

    @Test
    @DisplayName("빈 페이지 처리")
    void toPlainText_HandlesEmptyPages() {
        // given
        ClovaOcrResponse.ImageResult emptyImage = new ClovaOcrResponse.ImageResult(
                "img1", "page1", "SUCCESS", null, List.of()
        );
        ClovaOcrResponse response = ClovaOcrResponse.create("V2", "req1", List.of(emptyImage));

        // when
        String result = OcrTextExtractor.toPlainText(response);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("toPages 메서드 테스트")
    void toPages_ReturnsListOfPageTexts() {
        // given
        ClovaOcrResponse.ImageResult.Field field1 = new ClovaOcrResponse.ImageResult.Field(
                "text", "Page1", 0.95, "normal", true
        );
        ClovaOcrResponse.ImageResult.Field field2 = new ClovaOcrResponse.ImageResult.Field(
                "text", "Page2", 0.95, "normal", true
        );
        ClovaOcrResponse.ImageResult image1 = new ClovaOcrResponse.ImageResult(
                "img1", "page1", "SUCCESS", null, List.of(field1)
        );
        ClovaOcrResponse.ImageResult image2 = new ClovaOcrResponse.ImageResult(
                "img2", "page2", "SUCCESS", null, List.of(field2)
        );
        ClovaOcrResponse response = ClovaOcrResponse.create("V2", "req1", List.of(image1, image2));

        // when
        List<String> pages = OcrTextExtractor.toPages(response);

        // then
        assertThat(pages).hasSize(2);
        assertThat(pages.get(0)).isEqualTo("Page1");
        assertThat(pages.get(1)).isEqualTo("Page2");
    }
}