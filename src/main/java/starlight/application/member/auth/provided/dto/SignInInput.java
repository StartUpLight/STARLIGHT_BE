package starlight.application.member.auth.provided.dto;

public record SignInInput(
        String email,
        String password
) {
}
