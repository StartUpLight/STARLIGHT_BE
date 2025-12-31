package starlight.application.member.auth.provided.dto;

public record SignUpCommand(
        String name,
        String email,
        String phoneNumber,
        String password
) {
}
