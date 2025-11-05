package starlight.adapter.member.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import starlight.domain.member.entity.Credential;

public interface CredentialRepository extends JpaRepository<Credential, Long> {
}
