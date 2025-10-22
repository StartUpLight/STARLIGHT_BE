package starlight.adapter.prompt.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import starlight.application.prompt.required.PromptFinder;
import starlight.shared.apiPayload.exception.GlobalErrorType;
import starlight.shared.apiPayload.exception.GlobalException;

@Component
@RequiredArgsConstructor
public class PromptQueryService implements PromptFinder {

    private final PromptRepository promptRepository;

    @Override
    public String findPromptByTag(String tag) {
        return promptRepository.findContentByTag(tag)
                .orElseThrow(() -> new GlobalException(GlobalErrorType.INTERNAL_ERROR));
    }
}
