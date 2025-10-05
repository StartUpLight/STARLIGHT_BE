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
        Map<String, Object> a = o.getAttributes();

        return switch (registrationId) {
            case "google" -> new Parsed("google",
                    (String) a.get("sub"),
                    (String) a.get("email"),
                    (String) a.getOrDefault("name",""), " ",
                    a, "sub");
            case "naver" -> {
                Map<String,Object> resp = (Map<String,Object>) a.get("response");
                yield new Parsed("naver",
                        (String) resp.get("id"),
                        (String) resp.get("email"),
                        (String) resp.getOrDefault("name",""), " ",
                        a, "response");
            }
            case "kakao" -> {
                String id = String.valueOf(a.get("id"));
                Map<String,Object> account = (Map<String,Object>) a.get("kakao_account");
                String email = account == null ? null : (String) account.get("email");
                Map<String, Object> profile = account == null ? null : (Map<String, Object>) account.get("profile");
                String name = profile == null ? "" : (String) profile.getOrDefault("nickname", "");
                String profileImage = profile == null ? "" : (String) profile.getOrDefault("profile_image_url", "");
                yield new Parsed("kakao", id, email, name, profileImage, a, "id");
            }
            default -> new Parsed(registrationId, null, null, null, " ", a, "id");
        };
    }
}
