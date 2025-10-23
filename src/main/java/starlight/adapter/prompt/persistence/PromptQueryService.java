package starlight.adapter.prompt.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import starlight.application.prompt.required.PromptFinder;
import starlight.shared.apiPayload.exception.GlobalErrorType;
import starlight.shared.apiPayload.exception.GlobalException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PromptQueryService implements PromptFinder {

    private final PromptRepository promptRepository;

    @Override
    public List<String> getSectionCriteria(String tag) {
        List<String> contents = promptRepository.findContentsByTag(tag);

        if (contents == null || contents.isEmpty()) {
            throw new GlobalException(GlobalErrorType.INTERNAL_ERROR);
        }

        return contents;
    }
}
