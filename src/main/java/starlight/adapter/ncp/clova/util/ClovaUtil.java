package starlight.adapter.ncp.clova.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ClovaUtil {

    public static Map<String, Object> buildClovaRequestBody(String systemMsg, String userMsg, int n){
        Map<String, Object> props = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        for (int i = 1; i <= n; i++) {
            String key = "c" + i;
            props.put(key, Map.of("type", "boolean"));
            required.add(key);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemMsg),
                Map.of("role", "user", "content", userMsg)
        ));
        body.put("thinking", Map.of("effort", "none"));
        body.put("responseFormat", Map.of(
                "type", "json",
                "schema", Map.of(
                        "type", "array",
                        "items", Map.of("type", "boolean"),
                        "minItems", n,
                        "maxItems", n
                )
        ));
        // 필요 시 파라미터 사용
        // body.put("temperature", 0.0);
        // body.put("topP", 0.9);
        // body.put("topK", 0);
        // body.put("repetitionPenalty", 1.1);
        // body.put("maxCompletionTokens", Math.max(64, n * 8)); // maxTokens 금지

        return body;
    }

    public static String buildUserContent(String input, List<String> criteria) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[CHECKLIST]\n");

        for (int i = 0; i < criteria.size(); i++) {
            stringBuilder.append(i + 1).append(") ").append(criteria.get(i)).append("\n");
        }
        stringBuilder.append("\n[INPUT]\n").append(input);
        stringBuilder.append("\n\n[REQUEST]\n").append("위의 CHECKLIST 항목에 대해 각각 TRUE 또는 FALSE로 답변해 주세요. 답변은 JSON 배열 형식으로 제공해 주세요.");
        return stringBuilder.toString();
    }

    public static List<Boolean> toBooleanList(String contentJson, int n) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try {
            root = mapper.readTree(contentJson);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid JSON from Clova: " + contentJson, e);
        }

        List<Boolean> checks = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            JsonNode node = root.get(i);
            if (node == null || !node.isBoolean()) {
                throw new IllegalStateException("Non-boolean value at index " + i);
            }
            checks.add(node.asBoolean());
        }

        return checks;
    }
}
