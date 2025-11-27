package starlight.application.businessplan.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlainTextExtractUtilsTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("content 배열에서 text/image/table를 순서대로 줄글로 변환")
    void extractPlainText_fromContentArray() {
        String json = "{" +
                "\"content\":[" +
                "{\"type\":\"text\",\"value\":\"Hello\"}," +
                "{\"type\":\"image\",\"caption\":\"cap\"}," +
                "{\"type\":\"table\",\"columns\":[{\"width\":100},{\"width\":200}]," +
                "\"rows\":[" +
                "[{\"content\":[{\"type\":\"text\",\"value\":\"1\"}],\"rowspan\":1,\"colspan\":1}," +
                "{\"content\":[{\"type\":\"text\",\"value\":\"2\"}],\"rowspan\":1,\"colspan\":1}]," +
                "[{\"content\":[{\"type\":\"text\",\"value\":\"3\"}],\"rowspan\":1,\"colspan\":1}," +
                "{\"content\":[{\"type\":\"text\",\"value\":\"4\"}],\"rowspan\":1,\"colspan\":1}]" +
                "]" +
                "}" +
                "]}";

        String result = PlainTextExtractUtils.extractPlainText(mapper, json);

        assertThat(result).isEqualTo(String.join("\n",
                "Hello",
                "[사진] cap",
                "[2 columns]",
                "[\"1\", \"2\"]",
                "[\"3\", \"4\"]"));
    }

    @Test
    @DisplayName("blocks 내부 content를 모두 모아 순서대로 변환")
    void extractPlainText_fromBlocks() {
        String json = "{" +
                "\"blocks\":[" +
                "{\"content\":[{\"type\":\"text\",\"value\":\"A\"}]}," +
                "{\"content\":[{\"type\":\"text\",\"value\":\"B\"}]}" +
                "]}";

        String result = PlainTextExtractUtils.extractPlainText(mapper, json);
        assertThat(result).isEqualTo("A\nB");
    }

    @Test
    @DisplayName("image 캡션 없으면 [사진]만 출력")
    void extractPlainText_imageWithoutCaption() {
        String json = "{" +
                "\"content\":[{\"type\":\"image\"}]" +
                "}";

        String result = PlainTextExtractUtils.extractPlainText(mapper, json);
        assertThat(result).isEqualTo("[사진]");
    }

    @Test
    @DisplayName("잘못된 JSON이면 예외")
    void extractPlainText_invalidJson_throws() {
        assertThrows(IllegalArgumentException.class, () -> PlainTextExtractUtils.extractPlainText(mapper, "not-json"));
    }

    @Test
    @DisplayName("테이블 셀에서 rowspan/colspan (lowercase) 지원")
    void extractPlainText_tableWithLowercaseSpans() {
        String json = "{" +
                "\"content\":[" +
                "{\"type\":\"table\",\"columns\":[{\"width\":100},{\"width\":200}]," +
                "\"rows\":[" +
                "[{\"content\":[{\"type\":\"text\",\"value\":\"A\"}],\"rowspan\":2,\"colspan\":1}," +
                "{\"content\":[{\"type\":\"text\",\"value\":\"B\"}],\"rowspan\":1,\"colspan\":1}]," +
                "[]" +
                "]" +
                "}" +
                "]}";

        String result = PlainTextExtractUtils.extractPlainText(mapper, json);

        assertThat(result).isEqualTo(String.join("\n",
                "[2 columns]",
                "[\"A\", \"B\"]",
                "[\"\", \"\"]"));
    }

    @Test
    @DisplayName("테이블 셀에서 rowSpan/colSpan (camelCase) 지원")
    void extractPlainText_tableWithCamelCaseSpans() {
        String json = "{" +
                "\"content\":[" +
                "{\"type\":\"table\",\"columns\":[{\"width\":100},{\"width\":200}]," +
                "\"rows\":[" +
                "[{\"content\":[{\"type\":\"text\",\"value\":\"X\"}],\"rowSpan\":2,\"colSpan\":1}," +
                "{\"content\":[{\"type\":\"text\",\"value\":\"Y\"}],\"rowSpan\":1,\"colSpan\":1}]," +
                "[]" +
                "]" +
                "}" +
                "]}";

        String result = PlainTextExtractUtils.extractPlainText(mapper, json);

        assertThat(result).isEqualTo(String.join("\n",
                "[2 columns]",
                "[\"X\", \"Y\"]",
                "[\"\", \"\"]"));
    }
}
