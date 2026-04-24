package starlight.adapter.notification.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import starlight.application.notification.required.NotificationRealtimePort;
import starlight.application.notification.required.dto.NotificationPublishMessage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class NotificationSseRegistry implements NotificationRealtimePort {

    @Value("${notification.sse.timeout-ms:0}")
    private Long sseTimeoutMs;

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(Long memberId) {
        SseEmitter emitter = new SseEmitter(sseTimeoutMs);
        emitters.computeIfAbsent(memberId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(memberId, emitter));
        emitter.onTimeout(() -> removeEmitter(memberId, emitter));
        emitter.onError(ignored -> removeEmitter(memberId, emitter));

        try {
            safeSend(
                    emitter,
                    SseEmitter.event()
                            .name("connected")
                            .data("ok", MediaType.TEXT_PLAIN)
            );
        } catch (IOException exception) {
            removeEmitter(memberId, emitter);
            emitter.completeWithError(exception);
        }

        return emitter;
    }

    @Override
    public void send(NotificationPublishMessage message) {
        List<SseEmitter> memberEmitters = emitters.get(message.memberId());
        if (memberEmitters == null || memberEmitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : memberEmitters) {
            try {
                safeSend(
                        emitter,
                        SseEmitter.event()
                                .id(String.valueOf(message.notificationId()))
                                .name("notification")
                                .data(message, MediaType.APPLICATION_JSON)
                );
            } catch (IOException exception) {
                log.warn("[NOTIFICATION] SSE send failed notificationId={}, memberId={}",
                        message.notificationId(), message.memberId(), exception);
                removeEmitter(message.memberId(), emitter);
            }
        }
    }

    @Scheduled(fixedDelayString = "${notification.sse.heartbeat-ms:30000}")
    public void sendHeartbeat() {
        for (Map.Entry<Long, List<SseEmitter>> entry : emitters.entrySet()) {
            Long memberId = entry.getKey();
            List<SseEmitter> memberEmitters = entry.getValue();

            if (memberEmitters == null || memberEmitters.isEmpty()) {
                emitters.remove(memberId);
                continue;
            }

            for (SseEmitter emitter : memberEmitters) {
                try {
                    safeSend(
                            emitter,
                            SseEmitter.event()
                                    .name("ping")
                                    .data("ok", MediaType.TEXT_PLAIN)
                    );
                } catch (IOException exception) {
                    log.debug("[NOTIFICATION] heartbeat failed memberId={}", memberId, exception);
                    removeEmitter(memberId, emitter);
                }
            }
        }
    }

    private void removeEmitter(Long memberId, SseEmitter emitter) {
        List<SseEmitter> memberEmitters = emitters.get(memberId);
        if (memberEmitters == null) {
            return;
        }

        memberEmitters.remove(emitter);
        if (memberEmitters.isEmpty()) {
            emitters.remove(memberId);
        }
    }

    private void safeSend(SseEmitter emitter, SseEmitter.SseEventBuilder event) throws IOException {
        synchronized (emitter) {
            emitter.send(event);
        }
    }
}
