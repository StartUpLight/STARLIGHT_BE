package starlight.adapter.aireport.report.circuitbreaker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import starlight.shared.enumerate.SectionType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class SectionGradingCircuitBreaker {
    
    private static final int FAILURE_THRESHOLD = 5;  // 5번 연속 실패 시 오픈
    private static final int SUCCESS_THRESHOLD = 2;  // 2번 연속 성공 시 클로즈
    private static final long HALF_OPEN_TIMEOUT_SECONDS = 60;  // 60초 후 하프오픈 시도
    
    private final Map<SectionType, CircuitState> circuitStates = new ConcurrentHashMap<>();
    
    public enum State {
        CLOSED,    // 정상 동작
        OPEN,      // 차단됨
        HALF_OPEN  // 테스트 중
    }
    
    private static class CircuitState {
        private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicReference<LocalDateTime> lastFailureTime = new AtomicReference<>();
        
        public boolean allowRequest() {
            State current = state.get();
            
            if (current == State.CLOSED) {
                return true;
            }
            
            if (current == State.OPEN) {
                // 타임아웃 체크하여 HALF_OPEN으로 전환
                LocalDateTime lastFailure = lastFailureTime.get();
                if (lastFailure != null && 
                    java.time.Duration.between(lastFailure, LocalDateTime.now())
                        .getSeconds() >= HALF_OPEN_TIMEOUT_SECONDS) {
                    if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                        successCount.set(0);
                        failureCount.set(0); // HALF_OPEN 전환 시 failureCount 리셋
                        lastFailureTime.set(LocalDateTime.now()); // lastFailureTime도 갱신
                        log.info("Circuit breaker transitioning to HALF_OPEN");
                        return true;
                    }
                }
                return false;
            }
            
            // HALF_OPEN 상태
            return true;
        }
        
        public void recordSuccess() {
            State current = state.get();
            if (current == State.HALF_OPEN) {
                int success = successCount.incrementAndGet();
                if (success >= SUCCESS_THRESHOLD) {
                    if (state.compareAndSet(State.HALF_OPEN, State.CLOSED)) {
                        failureCount.set(0);
                        log.info("Circuit breaker CLOSED after successful recovery");
                    }
                }
            } else if (current == State.CLOSED) {
                failureCount.set(0);  // 성공 시 실패 카운트 리셋
            }
        }
        
        public void recordFailure() {
            State current = state.get();
            if (current == State.CLOSED) {
                int failures = failureCount.incrementAndGet();
                lastFailureTime.set(LocalDateTime.now());
                
                if (failures >= FAILURE_THRESHOLD) {
                    if (state.compareAndSet(State.CLOSED, State.OPEN)) {
                        log.warn("Circuit breaker OPENED after {} failures", failures);
                    }
                }
            } else if (current == State.HALF_OPEN) {
                // HALF_OPEN에서는 첫 실패 시 즉시 OPEN으로 전환
                lastFailureTime.set(LocalDateTime.now());
                if (state.compareAndSet(State.HALF_OPEN, State.OPEN)) {
                    log.warn("Circuit breaker OPENED after failure in HALF_OPEN state");
                }
            }
        }
    }
    
    public boolean allowRequest(SectionType sectionType) {
        CircuitState circuit = circuitStates.computeIfAbsent(
            sectionType, 
            k -> new CircuitState()
        );
        return circuit.allowRequest();
    }

    public void recordSuccess(SectionType sectionType) {
        CircuitState circuit = circuitStates.computeIfAbsent(
                sectionType,
                k -> new CircuitState());
        circuit.recordSuccess();
    }

    public void recordFailure(SectionType sectionType, String errorMessage) {
        CircuitState circuit = circuitStates.computeIfAbsent(
                sectionType,
                k -> new CircuitState());
        circuit.recordFailure();
    }

    public void recordFailure(SectionType sectionType) {
        recordFailure(sectionType, null);
    }
}
