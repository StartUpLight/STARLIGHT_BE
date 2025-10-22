package starlight.adapter.prompt.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.domain.prompt.entity.Prompt;

import java.util.Optional;

public interface PromptRepository extends JpaRepository<Prompt, Long> {

    @Query("SELECT p.content FROM Prompt p WHERE p.tag = :tag")
    Optional<String> findContentByTag(@Param("tag") String tag);
}
