package starlight.adapter.expert.webapi;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import starlight.adapter.expert.webapi.dto.ExpertDetailResponse;
import starlight.adapter.expert.webapi.dto.ExpertListResponse;
import starlight.adapter.expert.webapi.swagger.ExpertQueryApiDoc;
import starlight.application.expert.provided.ExpertDetailQueryUseCase;
import starlight.domain.expert.enumerate.TagCategory;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/experts")
public class ExpertController implements ExpertQueryApiDoc {

    private final ExpertDetailQueryUseCase expertDetailQuery;

    @GetMapping
    public ApiResponse<List<ExpertListResponse>> search(
            @RequestParam(name = "categories", required = false) Set<TagCategory> categories
    ) {
        return ApiResponse.success(ExpertListResponse.fromAll(expertDetailQuery.search(categories)));
    }

    @GetMapping("/{expertId}")
    public ApiResponse<ExpertDetailResponse> detail(
            @PathVariable Long expertId
    ) {
        return ApiResponse.success(ExpertDetailResponse.from(expertDetailQuery.findById(expertId)));
    }
}
