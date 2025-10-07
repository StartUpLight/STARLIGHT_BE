package starlight.shared.apiPayload.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionUnitTest {

    @Test
    void 생성자_에러타입_메시지_정상_세팅() {
        ErrorType errorType = GlobalErrorType.INTERNAL_ERROR;
        GlobalException exception = new GlobalException(errorType);

        assertThat(exception.getErrorType()).isEqualTo(errorType);
        assertThat(exception.getMessage()).isEqualTo(errorType.getMessage());
    }
}