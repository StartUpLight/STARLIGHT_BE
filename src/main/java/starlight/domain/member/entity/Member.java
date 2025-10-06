package starlight.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import starlight.domain.member.enumerate.MemberType;
import starlight.domain.member.exception.MemberErrorType;
import starlight.domain.member.exception.MemberException;
import starlight.shared.AbstractEntity;

@Getter
@Entity
@SQLDelete(sql = "UPDATE \"member\" SET deleted_at = NOW() WHERE id = ?")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends AbstractEntity {

    @Column(columnDefinition = "varchar(320)")
    private String name;

    @Column(nullable = false, columnDefinition = "varchar(320)")
    private String email;

    @Column(columnDefinition = "varchar(20)")
    private String phoneNumber;

    @Column(nullable = false, columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private MemberType memberType;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "credential_id", referencedColumnName = "id")
    private Credential credential;

    @Column(length = 20)
    private String provider;

    @Column(length = 64)
    private String providerId;

    public static Member create(String name, String email, String phoneNumber, MemberType memberType, Credential credential) {
        Member member = new Member();
        member.name = name;
        member.email = email;
        member.phoneNumber = phoneNumber;
        member.memberType = memberType != null ? memberType : MemberType.WRITER;
        member.credential = credential;
        return member;
    }

    public static Member newSocial(String name, String email, String provider,
                                   String providerId, String phoneNumber, MemberType memberType) {
        Member member = Member.create(name, email, phoneNumber, memberType, null);

        member.provider = provider;
        member.providerId = providerId;

        return member;
    }
}
