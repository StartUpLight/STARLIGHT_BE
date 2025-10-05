package starlight.domain.member.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberType {
    WRITER("사용자"),
    EXPERT("전문가");

    private final String role;
}