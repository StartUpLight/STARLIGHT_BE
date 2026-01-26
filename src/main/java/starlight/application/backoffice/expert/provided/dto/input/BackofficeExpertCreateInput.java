package starlight.application.backoffice.expert.provided.dto.input;

import starlight.domain.expert.enumerate.TagCategory;

import java.util.List;

public record BackofficeExpertCreateInput(
        String name,
        String email,
        String oneLineIntroduction,
        List<String> tags,
        List<TagCategory> categories
) {
    public static BackofficeExpertCreateInput of(
            String name,
            String email,
            String oneLineIntroduction,
            List<String> tags,
            List<TagCategory> categories
    ) {
        return new BackofficeExpertCreateInput(name, email, oneLineIntroduction, tags, categories);
    }
}
