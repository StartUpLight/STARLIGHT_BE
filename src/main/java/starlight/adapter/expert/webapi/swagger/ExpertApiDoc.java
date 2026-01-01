package starlight.adapter.expert.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import starlight.adapter.expert.webapi.dto.ExpertAiReportBusinessPlanResponse;
import starlight.adapter.expert.webapi.dto.ExpertDetailResponse;
import starlight.adapter.expert.webapi.dto.ExpertListResponse;
import starlight.shared.apiPayload.response.ApiResponse;
import starlight.shared.auth.AuthenticatedMember;

import java.util.List;

@Tag(name = "전문가", description = "전문가 관련 API")
public interface ExpertApiDoc {

    @Operation(
            summary = "전문가 목록 조회",
            description = "전체 전문가 목록을 반환합니다."
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
                              "oneLineIntroduction": "한 줄 소개",
                              "profileImageUrl": "https://cdn.example.com/profiles/1.png",
                              "workedPeriod": 6,
                              "email": "hong@example.com",
                              "careers": [
                                { "orderIndex": 0, "careerTitle": "A사 PO (2019-2022)" },
                                { "orderIndex": 1, "careerTitle": "B스타트업 PM (2023-)" }
                              ],
                              "tags": ["B2B", "SaaS", "PM"],
                              "categories": ["성장 전략","팀 역량"]
                            },
                            {
                              "id": 2,
                              "name": "이영희",
                              "oneLineIntroduction": "한 줄 소개",
                              "profileImageUrl": "https://cdn.example.com/profiles/2.png",
                              "workedPeriod": 4,
                              "email": "lee@example.com",
                              "careers": [
                                { "orderIndex": 0, "careerTitle": "C기업 데이터분석 (2020-)" }
                              ],
                              "tags": ["데이터", "분석"],
                              "categories": ["시장성/BM","지표/데이터"]
                            }
                          ],
                          "error": null
                        }
                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "전문가 조회 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "EXPERT_QUERY_ERROR",
                            "message": "전문가 정보를 조회하는 중에 오류가 발생했습니다."
                          }
                        }
                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "신청 건수 조회 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "EXPERT_APPLICATION_QUERY_ERROR",
                            "message": "전문가 신청 정보를 조회하는 중에 오류가 발생했습니다."
                          }
                        }
                        """
                            )
                    )
            )
    })
    @GetMapping
    ApiResponse<List<ExpertListResponse>> search();

    @Operation(summary = "전문가 상세 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExpertDetailResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "전문가 조회 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "전문가 없음",
                                            value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "EXPERT_NOT_FOUND",
                            "message": "해당 전문가를 찾을 수 없습니다."
                          }
                        }
                        """
                                    ),
                                    @ExampleObject(
                                            name = "조회 오류",
                                            value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "EXPERT_QUERY_ERROR",
                            "message": "전문가 정보를 조회하는 중에 오류가 발생했습니다."
                          }
                        }
                        """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "신청 건수 조회 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "EXPERT_APPLICATION_QUERY_ERROR",
                            "message": "전문가 신청 정보를 조회하는 중에 오류가 발생했습니다."
                          }
                        }
                        """
                            )
                    )
            )
    })
    @GetMapping("/{expertId}")
    ApiResponse<ExpertDetailResponse> detail(
            @PathVariable Long expertId
    );

    @Operation(
            summary = "전문가 상세 내 AI 리포트 보유 사업계획서 목록",
            description = "지정된 전문가의 전문가 상세 페이지에서 로그인한 사용자의 사업계획서 중 AI 리포트가 생성된 항목만 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ExpertAiReportBusinessPlanResponse.class)),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                        {
                          "result": "SUCCESS",
                          "data": [
                            {
                              "businessPlanId": 10,
                              "businessPlanTitle": "테스트 사업계획서",
                              "requestCount": 2,
                              "isOver70": true
                            },
                            {
                              "businessPlanId": 11,
                              "businessPlanTitle": "신규 사업계획서",
                              "requestCount": 0,
                              "isOver70": false
                            }
                          ],
                          "error": null
                        }
                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "조회 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "전문가 신청 조회 오류",
                                            value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "EXPERT_APPLICATION_QUERY_ERROR",
                            "message": "전문가 신청 정보를 조회하는 중에 오류가 발생했습니다."
                          }
                        }
                        """
                                    ),
                                    @ExampleObject(
                                            name = "AI 리포트 파싱 오류",
                                            value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "AI_RESPONSE_PARSING_FAILED",
                            "message": "AI 응답 파싱에 실패했습니다."
                          }
                        }
                        """
                                    )
                            }
                    )
            )
    })
    @GetMapping("/{expertId}/business-plans/ai-reports")
    ApiResponse<List<ExpertAiReportBusinessPlanResponse>> aiReportBusinessPlans(
            @PathVariable Long expertId,
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    );
}
