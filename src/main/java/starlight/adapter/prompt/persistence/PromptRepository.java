package starlight.adapter.prompt.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.domain.prompt.entity.Prompt;

import java.util.List;

public interface PromptRepository extends JpaRepository<Prompt, Long> {

    @Query("SELECT p.content FROM Prompt p WHERE p.tag = :tag")
    List<String> findContentsByTag(@Param("tag") String tag);
}
