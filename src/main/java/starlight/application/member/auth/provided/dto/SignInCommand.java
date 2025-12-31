package starlight.application.member.auth.provided.dto;

public record SignInCommand(
        String email,
        String password
) {
}
