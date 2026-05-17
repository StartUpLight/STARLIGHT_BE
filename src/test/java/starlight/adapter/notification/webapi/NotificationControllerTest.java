package starlight.adapter.notification.webapi;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import starlight.application.notification.provided.NotificationUseCase;
import starlight.shared.auth.AuthenticatedMember;
import starlight.shared.apiPayload.exception.GlobalException;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationControllerTest {

    private final NotificationUseCase notificationUseCase = mock(NotificationUseCase.class);
    private final NotificationController notificationController = new NotificationController(notificationUseCase);

    @Test
    void subscribe_LastEventId헤더를_쿼리파라미터보다_우선한다() {
        SseEmitter emitter = new SseEmitter();
        AuthenticatedMember member = new TestAuthenticatedMember(1L, "tester");

        when(notificationUseCase.subscribe(1L, 20L)).thenReturn(emitter);

        SseEmitter result = notificationController.subscribe(member, 20L, 10L);

        assertSame(emitter, result);
        verify(notificationUseCase).subscribe(1L, 20L);
    }

    @Test
    void subscribe_LastEventId헤더가_없으면_쿼리파라미터를_사용한다() {
        SseEmitter emitter = new SseEmitter();
        AuthenticatedMember member = new TestAuthenticatedMember(1L, "tester");

        when(notificationUseCase.subscribe(1L, 10L)).thenReturn(emitter);

        SseEmitter result = notificationController.subscribe(member, null, 10L);

        assertSame(emitter, result);
        verify(notificationUseCase).subscribe(1L, 10L);
    }

    @Test
    void subscribe_인증정보가_없으면_예외가_발생한다() {
        assertThrows(GlobalException.class, () -> notificationController.subscribe(null, null, null));
    }

    private record TestAuthenticatedMember(Long memberId, String memberName) implements AuthenticatedMember {
        @Override
        public Long getMemberId() {
            return memberId;
        }

        @Override
        public String getMemberName() {
            return memberName;
        }
    }
}
