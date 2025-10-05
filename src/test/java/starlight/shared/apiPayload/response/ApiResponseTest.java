package starlight.shared.apiPayload.response;

import org.junit.jupiter.api.Test;
import starlight.shared.apiPayload.exception.GlobalErrorType;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void success_without_data() {

        ApiResponse<?> response = ApiResponse.success();

        assertThat(response.result()).isEqualTo(ResultType.SUCCESS);
        assertThat(response.data()).isNull();
        assertThat(response.error()).isNull();
    }

    @Test
    void success_with_data() {
        String data = "테스트 데이터";

        ApiResponse<String> response = ApiResponse.success(data);

        assertThat(response.result()).isEqualTo(ResultType.SUCCESS);
        assertThat(response.data()).isEqualTo(data);
        assertThat(response.error()).isNull();
    }

    @Test
    void error_response() {

        ApiResponse<?> response = ApiResponse.error(GlobalErrorType.INTERNAL_ERROR);

        assertThat(response.result()).isEqualTo(ResultType.ERROR);
        assertThat(response.data()).isNull();
        assertThat(response.error()).isNotNull();
        assertThat(response.error().getCode()).isEqualTo(GlobalErrorType.INTERNAL_ERROR.name());
    }
}
