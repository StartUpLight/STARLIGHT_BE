package starlight.adapter.expertReport.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import starlight.adapter.expertReport.webapi.dto.ExpertReportResponse;
import starlight.adapter.expertReport.webapi.dto.UpsertExpertReportRequest;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@Tag(name = "전문가 리포트", description = "전문가 피드백 리포트 API")
public interface ExpertReportApiDoc {

    @Operation(summary = "전문가 리포트 목록 조회 (사용자)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ExpertReportResponse.class))
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사업계획서 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "BUSINESS_PLAN_NOT_FOUND",
                            "message": "해당 사업계획서가 존재하지 않습니다."
                          }
                        }
                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "전문가 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
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
                            )
                    )
            )
    })
    @GetMapping
    ApiResponse<List<ExpertReportResponse>> getExpertReports(
            @RequestParam Long businessPlanId
    );

    @Operation(summary = "전문가 리포트 단건 조회 (전문가)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExpertReportResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "전문가 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
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
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "리포트 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "EXPERT_REPORT_NOT_FOUND",
                            "message": "해당 전문가 리포트를 찾을 수 없습니다."
                          }
                        }
                        """
                            )
                    )
            )
    })
    @GetMapping("/{token}")
    ApiResponse<ExpertReportResponse> getExpertReport(
            @PathVariable String token
    );

    @Operation(summary = "전문가 리포트 저장 (전문가)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExpertReportResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "이미 제출됨",
                                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "ALREADY_SUBMITTED",
                                "message": "이미 전문가 피드백을 제출하였습니다."
                              }
                            }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "요청 만료",
                                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "REPORT_EXPIRED",
                                "message": "전문가 피드백 요청 기간이 만료되었습니다."
                              }
                            }
                            """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사업계획서 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "BUSINESS_PLAN_NOT_FOUND",
                            "message": "해당 사업계획서가 존재하지 않습니다."
                          }
                        }
                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "리포트 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                        {
                          "result": "ERROR",
                          "data": null,
                          "error": {
                            "code": "EXPERT_REPORT_NOT_FOUND",
                            "message": "해당 전문가 리포트를 찾을 수 없습니다."
                          }
                        }
                        """
                            )
                    )
            )
    })
    @PostMapping("/{token}")
    ApiResponse<ExpertReportResponse> save(
            @PathVariable String token,
            @Valid @RequestBody UpsertExpertReportRequest request
    );
}
