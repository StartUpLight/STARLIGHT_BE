package starlight.adapter.notification.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import starlight.adapter.notification.webapi.dto.request.NotificationTestRequest;
import starlight.adapter.notification.webapi.dto.response.NotificationResponse;
import starlight.shared.auth.AuthenticatedMember;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@Tag(name = "알림", description = "알림 SSE 및 알림 조회 API")
@SecurityRequirement(name = "bearerAuth")
public interface NotificationApiDoc {

    @Operation(summary = "알림 SSE를 구독합니다.")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter subscribe(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    );

    @Operation(summary = "내 알림 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공"
            )
    })
    @GetMapping
    ApiResponse<List<NotificationResponse>> findMyNotifications(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    );

    @Operation(summary = "알림을 읽음 처리합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "result": "ERROR",
                                              "data": null,
                                              "error": {
                                                "code": "NOTIFICATION_ACCESS_DENIED",
                                                "message": "해당 알림에 접근할 수 없습니다."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "알림 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "result": "ERROR",
                                              "data": null,
                                              "error": {
                                                "code": "NOTIFICATION_NOT_FOUND",
                                                "message": "해당 알림을 찾을 수 없습니다."
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    @PatchMapping("/{notificationId}/read")
    ApiResponse<?> readNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    );

    @Operation(summary = "현재 로그인한 사용자에게 테스트 알림을 전송합니다.")
    @PostMapping("/test")
    ApiResponse<?> sendTestNotification(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @Valid @RequestBody NotificationTestRequest request
    );
}
