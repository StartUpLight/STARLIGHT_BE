package starlight.adapter.auth.security.oauth2;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

public final class OAuth2Attributes {

    private OAuth2Attributes() {}

    public record Parsed(
            String provider, String providerId,
            String email, String name,
            String profileImageUrl,
            Map<String,Object> attributes, String nameAttributeKey
    ) {}

    @SuppressWarnings("unchecked")
    public static Parsed parse(OAuth2UserRequest req, OAuth2User o) {
        String registrationId = req.getClientRegistration().getRegistrationId(); // google/naver/kakao
        Map<String, Object> attributes = o.getAttributes();

        return switch (registrationId) {
            case "naver" -> {
                Map<String,Object> response = (Map<String,Object>) attributes.get("response");
                if (response == null) response = Map.of();

                String id           = String.valueOf(response.getOrDefault("id", ""));
                String email        = (String) response.get("email");
                String name         = (String) (response.getOrDefault("name", response.getOrDefault("nickname", "")));
                String profileImage = (String) response.getOrDefault("profile_image", "");

                yield new Parsed("naver", id, email, name, profileImage, attributes, "id");
            }
            case "kakao" -> {
                Map<String,Object> response = (Map<String,Object>) attributes.get("kakao_account");
                if (response == null) response = Map.of();

                String id           = String.valueOf(attributes.getOrDefault("id", ""));
                String email        = (String) response.get("email");
                String name         = (String) ((Map<String, Object>) response.getOrDefault("profile", Map.of())).getOrDefault("nickname", "");
                String profileImage = (String) ((Map<String, Object>) response.getOrDefault("profile", Map.of())).getOrDefault("profile_image_url", "");

                yield new Parsed("kakao", id, email, name, profileImage, attributes, "id");
            }
            default -> new Parsed(registrationId, null, null, null, " ", attributes, "id");
        };
    }
}
