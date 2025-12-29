package starlight.adapter.expert.webapi;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import starlight.adapter.expert.webapi.dto.ExpertDetailResponse;
import starlight.adapter.expert.webapi.dto.ExpertListResponse;
import starlight.adapter.expert.webapi.swagger.ExpertQueryApiDoc;
import starlight.application.expert.provided.ExpertDetailQueryUseCase;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/experts")
public class ExpertController implements ExpertQueryApiDoc {

    private final ExpertDetailQueryUseCase expertDetailQuery;

    @GetMapping
    public ApiResponse<List<ExpertListResponse>> search() {
        return ApiResponse.success(ExpertListResponse.fromAll(expertDetailQuery.searchAll()));
    }

    @GetMapping("/{expertId}")
    public ApiResponse<ExpertDetailResponse> detail(
            @PathVariable Long expertId
    ) {
        return ApiResponse.success(ExpertDetailResponse.from(expertDetailQuery.findById(expertId)));
    }
}
