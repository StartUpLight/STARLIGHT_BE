package starlight.adapter.expert.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import starlight.adapter.expert.dto.ExpertListResponse;
import starlight.domain.expert.enumerate.TagCategory;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;
import java.util.Set;

@Tag(name = "전문가", description = "전문가 API")
public interface ExpertQueryApiDoc {

    @Operation(
            summary = "전문가 검색(AND 매칭)",
            description = """
            카테고리 파라미터가 없으면 전체 전문가를 반환합니다.
            \n카테고리를 하나 이상 전달하면 **전달된 모든 카테고리**를 보유한 전문가만 반환합니다(AND 매칭).
            \n MARKET_BM: 시장성/BM,  TEAM_CAPABILITY: 팀 역량, PROBLEM_DEFINITION: 문제 정의, GROWTH_STRATEGY: 성장 전략, METRIC_DATA: 지표/데이터 
            \nSwagger UI에서는 'Add item'으로 항목을 추가하면 ?categories=A&categories=B 형태로 전송됩니다.
            \n예) GET /v1/experts?categories=GROWTH_STRATEGY&categories=TEAM_CAPABILITY 
            \n예) GET /v1/experts?categories=GROWTH_STRATEGY,TEAM_CAPABILITY
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ExpertListResponse.class)),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                        {
                          "result": "SUCCESS",
                          "data": [
                            {
                              "id": 1,
                              "name": "홍길동",
                              "profileImageUrl": "https://cdn.example.com/profiles/1.png",
                              "email": "hong@example.com",
                              "mentoringPriceWon": 50000,
                              "careers": ["A사 PO (2019-2022)","B스타트업 PM (2023-)"],
                              "categories": ["성장 전략","팀 역량"]
                            },
                            {
                              "id": 2,
                              "name": "이영희",
                              "profileImageUrl": "https://cdn.example.com/profiles/2.png",
                              "email": "lee@example.com",
                              "mentoringPriceWon": 70000,
                              "careers": ["C기업 데이터분석 (2020-)"],
                              "categories": ["시장성/BM","지표/데이터"]
                            }
                          ],
                          "error": null
                        }
                        """
                            )
                    )
            ),
    })
    @GetMapping
    ApiResponse<List<ExpertListResponse>> search(
            @RequestParam(name = "categories", required = false)
            Set<TagCategory> categories
    );
}
