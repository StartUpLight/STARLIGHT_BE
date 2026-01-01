package starlight.application.member.auth.provided.dto;

public record SignUpInput(
        String name,
        String email,
        String phoneNumber,
        String password
) {
}
