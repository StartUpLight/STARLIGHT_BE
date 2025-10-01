package starlight.global.apiPayload.response;

import starlight.global.apiPayload.exception.ErrorMessage;
import starlight.global.apiPayload.exception.ErrorType;

public record ApiResponse<T>(ResultType result, T data, ErrorMessage error) {

    public static ApiResponse<?> success() {
        return new ApiResponse<>(ResultType.SUCCESS, null, null);
    }

    public static <S> ApiResponse<S> success(S data) {
        return new ApiResponse<>(ResultType.SUCCESS, data, null);
    }

    public static ApiResponse<?> error(ErrorType error) {
        return new ApiResponse<>(ResultType.ERROR, null, new ErrorMessage(error));
    }

}