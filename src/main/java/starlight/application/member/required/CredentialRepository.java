package starlight.application.member.required;

import org.springframework.data.jpa.repository.JpaRepository;
import starlight.domain.member.entity.Credential;

public interface CredentialRepository extends JpaRepository<Credential, Long> {
}
