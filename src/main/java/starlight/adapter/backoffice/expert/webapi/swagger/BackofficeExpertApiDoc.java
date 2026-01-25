package starlight.adapter.backoffice.expert.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import starlight.adapter.backoffice.expert.webapi.dto.request.BackofficeExpertActiveStatusUpdateRequest;
import starlight.adapter.backoffice.expert.webapi.dto.request.BackofficeExpertCreateRequest;
import starlight.adapter.backoffice.expert.webapi.dto.request.BackofficeExpertProfileImageUpdateRequest;
import starlight.adapter.backoffice.expert.webapi.dto.request.BackofficeExpertUpdateRequest;
import starlight.adapter.backoffice.expert.webapi.dto.response.BackofficeExpertCreateResponse;
import starlight.adapter.backoffice.expert.webapi.dto.response.BackofficeExpertDetailResponse;
import starlight.adapter.backoffice.expert.webapi.dto.response.BackofficeExpertListResponse;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@Tag(name = "[Office] 전문가", description = "백오피스 전문가 관리 API")
public interface BackofficeExpertApiDoc {

    @Operation(
            summary = "전문가 목록 조회(백오피스)",
            security = @SecurityRequirement(name = "backofficeSession")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = BackofficeExpertListResponse.class))
                    )
            )
    })
    @GetMapping
    ApiResponse<List<BackofficeExpertListResponse>> searchAll();

    @Operation(
            summary = "전문가 생성(백오피스)",
            security = @SecurityRequirement(name = "backofficeSession")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = BackofficeExpertCreateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 오류",
                    content = @Content(examples = @ExampleObject(
                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "INVALID_REQUEST_ARGUMENT",
                                "message": "잘못된 요청 인자입니다."
                              }
                            }
                            """
                    ))
            )
    })
    @PostMapping
    ApiResponse<BackofficeExpertCreateResponse> create(
            @RequestBody BackofficeExpertCreateRequest request
    );

    @Operation(
            summary = "전문가 상세 조회(백오피스)",
            security = @SecurityRequirement(name = "backofficeSession")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(implementation = BackofficeExpertDetailResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "전문가 조회 실패",
                    content = @Content(examples = {
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
                    })
            )
    })
    @GetMapping("/{expertId}")
    ApiResponse<BackofficeExpertDetailResponse> detail(
            @PathVariable Long expertId
    );

    @Operation(
            summary = "전문가 상세 수정(백오피스)",
            security = @SecurityRequirement(name = "backofficeSession")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 오류",
                    content = @Content(examples = @ExampleObject(
                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "INVALID_REQUEST_ARGUMENT",
                                "message": "잘못된 요청 인자입니다."
                              }
                            }
                            """
                    ))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "전문가 조회 실패",
                    content = @Content(examples = {
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
                    })
            )
    })
    @PatchMapping("/{expertId}")
    ApiResponse<?> update(
            @PathVariable Long expertId,
            @RequestBody BackofficeExpertUpdateRequest request
    );

    @Operation(
            summary = "전문가 활성 상태 변경(백오피스)",
            security = @SecurityRequirement(name = "backofficeSession")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공"
            )
    })
    @PatchMapping("/{expertId}/active-status")
    ApiResponse<?> updateActiveStatus(
            @PathVariable Long expertId,
            @RequestBody BackofficeExpertActiveStatusUpdateRequest request
    );

    @Operation(
            summary = "전문가 프로필 이미지 변경(백오피스)",
            security = @SecurityRequirement(name = "backofficeSession")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 값 오류",
                    content = @Content(examples = @ExampleObject(
                            value = """
                            {
                              "result": "ERROR",
                              "data": null,
                              "error": {
                                "code": "INVALID_REQUEST_ARGUMENT",
                                "message": "잘못된 요청 인자입니다."
                              }
                            }
                            """
                    ))
            )
    })
    @PatchMapping("/{expertId}/profile-image")
    ApiResponse<?> updateProfileImage(
            @PathVariable Long expertId,
            @RequestBody BackofficeExpertProfileImageUpdateRequest request
    );

    @Operation(
            summary = "전문가 삭제(백오피스)",
            security = @SecurityRequirement(name = "backofficeSession")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "전문가 조회 실패",
                    content = @Content(examples = @ExampleObject(
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
                    ))
            )
    })
    @DeleteMapping("/{expertId}")
    ApiResponse<?> delete(
            @PathVariable Long expertId
    );
}
