package starlight.shared.apiPayload;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import starlight.shared.apiPayload.exception.GlobalErrorType;
import starlight.shared.apiPayload.exception.GlobalException;
import starlight.shared.apiPayload.response.ApiResponse;
import starlight.shared.apiPayload.response.ResultType;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiControllerAdviceUnitTest {

    private ApiControllerAdvice sut;

    @BeforeEach
    void setUp() {
        sut = new ApiControllerAdvice();
    }

    @Test
    @DisplayName("일반 Exception 발생 시 INTERNAL_ERROR 응답")
    void handleException() {
        // given
        Exception exception = new Exception("예상치 못한 에러");

        // when
        ResponseEntity<ApiResponse<?>> response = sut.handleException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(GlobalErrorType.INTERNAL_ERROR.getStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().result()).isEqualTo(ResultType.ERROR);
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().error()).isNotNull();
    }

    @Test
    @DisplayName("MethodArgumentNotValidException 발생 시 FAILED_REQUEST_VALIDATION 응답")
    void handleMethodArgumentNotValidException() throws NoSuchMethodException {
        // given
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldError()).thenReturn(
                new FieldError("object", "field", "validation failed")
        );

        Method method = this.getClass().getMethod("dummyMethod", String.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // when
        ResponseEntity<ApiResponse<?>> response = sut.handleMethodArgumentNotValidException(exception);

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(GlobalErrorType.FAILED_REQUEST_VALIDATION.getStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().result()).isEqualTo(ResultType.ERROR);
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().error()).isNotNull();
    }

    // MethodParameter 생성용 더미 메서드
    public void dummyMethod(String param) {
        // 테스트용 더미 메서드
    }

    @Test
    @DisplayName("IllegalArgumentException 발생 시 INVALID_REQUEST_ARGUMENT 응답")
    void handleIllegalArgumentException() {
        // given
        IllegalArgumentException exception = new IllegalArgumentException("잘못된 인자");

        // when
        ResponseEntity<ApiResponse<?>> response = sut.handleIllegalArgumentException(exception);

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(GlobalErrorType.INVALID_REQUEST_ARGUMENT.getStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().result()).isEqualTo(ResultType.ERROR);
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().error()).isNotNull();
    }

    @Test
    @DisplayName("GlobalException 발생 시 해당 ErrorType으로 응답")
    void handleGlobalException() {
        // given
        GlobalException exception = new GlobalException(GlobalErrorType.INTERNAL_ERROR);

        // when
        ResponseEntity<ApiResponse<?>> response = sut.handleGlobalException(exception);

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(GlobalErrorType.INTERNAL_ERROR.getStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().result()).isEqualTo(ResultType.ERROR);
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().error()).isNotNull();
    }

    @Test
    @DisplayName("GlobalException - 커스텀 ErrorType 처리")
    void handleGlobalException_withCustomErrorType() {
        // given
        GlobalException exception = new GlobalException(GlobalErrorType.INVALID_REQUEST_ARGUMENT);

        // when
        ResponseEntity<ApiResponse<?>> response = sut.handleGlobalException(exception);

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(GlobalErrorType.INVALID_REQUEST_ARGUMENT.getStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().result()).isEqualTo(ResultType.ERROR);
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().error()).isNotNull();
    }

    @Test
    @DisplayName("RuntimeException도 Exception 핸들러로 처리됨")
    void handleException_withRuntimeException() {
        // given
        RuntimeException exception = new RuntimeException("런타임 에러");

        // when
        ResponseEntity<ApiResponse<?>> response = sut.handleException(exception);

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(GlobalErrorType.INTERNAL_ERROR.getStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().result()).isEqualTo(ResultType.ERROR);
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().error()).isNotNull();
    }
}