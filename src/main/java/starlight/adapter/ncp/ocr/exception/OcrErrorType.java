package starlight.adapter.ncp.ocr.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import starlight.shared.apiPayload.exception.ErrorType;

@Getter
@RequiredArgsConstructor
public enum OcrErrorType implements ErrorType {
    PDF_SPLIT_ERROR(HttpStatus.BAD_REQUEST, "PDF 분할 실패"),
    PDF_DOWNLOAD_ERROR(HttpStatus.BAD_GATEWAY, "PDF 다운로드 실패"),
    OCR_CLIENT_ERROR(HttpStatus.BAD_GATEWAY, "CLOVA OCR 호출 실패"),
    PDF_EMPTY_RESPONSE(HttpStatus.BAD_GATEWAY, "PDF 응답이 비어있음"),
    OCR_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "CLOVA OCR 타임아웃/네트워크 실패"),
    PDF_TOO_LARGE(HttpStatus.INTERNAL_SERVER_ERROR, "PDF의 크기가 업로드 제한 크기를 넘습니다."),
    PAGE_COUNT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "페이지 수를 가져오는 데 실패했습니다."),
    ;

    private final HttpStatus status;

    private final String message;
}
