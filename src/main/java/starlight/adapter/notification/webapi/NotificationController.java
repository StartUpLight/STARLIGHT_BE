package starlight.adapter.notification.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import starlight.adapter.notification.webapi.dto.request.NotificationTestRequest;
import starlight.adapter.notification.webapi.dto.response.NotificationResponse;
import starlight.adapter.notification.webapi.swagger.NotificationApiDoc;
import starlight.application.notification.provided.NotificationUseCase;
import starlight.shared.auth.AuthenticatedMember;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/notifications")
public class NotificationController implements NotificationApiDoc {

    private final NotificationUseCase notificationUseCase;

    @Override
    @GetMapping("/subscribe")
    public SseEmitter subscribe(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    ) {
        return notificationUseCase.subscribe(authenticatedMember.getMemberId());
    }

    @Override
    @GetMapping
    public ApiResponse<List<NotificationResponse>> findMyNotifications(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    ) {
        return ApiResponse.success(NotificationResponse.fromAll(
                notificationUseCase.findAllByMemberId(authenticatedMember.getMemberId())
        ));
    }

    @Override
    @PatchMapping("/{notificationId}/read")
    public ApiResponse<?> readNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    ) {
        notificationUseCase.markAsRead(notificationId, authenticatedMember.getMemberId());
        return ApiResponse.success();
    }

    @Override
    @PostMapping("/test")
    public ApiResponse<?> sendTestNotification(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @Valid @RequestBody NotificationTestRequest request
    ) {
        notificationUseCase.notifyMember(request.toInput(authenticatedMember.getMemberId()));
        return ApiResponse.success();
    }
}
