package starlight.adapter.expert.webapi;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import starlight.adapter.expert.webapi.dto.ExpertDetailResponse;
import starlight.adapter.expert.webapi.swagger.ExpertQueryApiDoc;
import starlight.application.expert.provided.ExpertFinder;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.TagCategory;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/experts")
public class ExpertController implements ExpertQueryApiDoc {

    private final ExpertFinder expertFinder;

    @GetMapping
    public ApiResponse<List<ExpertDetailResponse>> search(
            @RequestParam(name = "categories", required = false) Set<TagCategory> categories
    ) {
        List<Expert> experts = (categories == null || categories.isEmpty())
                ? expertFinder.loadAll()
                : expertFinder.findByAllCategories(categories);

        return ApiResponse.success(ExpertDetailResponse.fromAll(experts));
    }
}
