package starlight.adapter.backoffice.expert.webapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import starlight.application.backoffice.expert.provided.dto.input.BackofficeExpertCreateInput;
import starlight.domain.expert.enumerate.TagCategory;

import java.util.List;

public record BackofficeExpertCreateRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        String oneLineIntroduction,
        List<String> tags,
        List<TagCategory> categories
) {
    public BackofficeExpertCreateInput toInput() {
        return BackofficeExpertCreateInput.of(
                name,
                email,
                oneLineIntroduction,
                tags,
                categories
        );
    }
}
