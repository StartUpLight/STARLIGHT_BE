package starlight.adapter.shared.infrastructure.pdf.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import starlight.shared.apiPayload.exception.ErrorType;

@Getter
@RequiredArgsConstructor
public enum PdfDownloadErrorType implements ErrorType {
    PDF_EMPTY_RESPONSE(HttpStatus.BAD_GATEWAY, "PDF 응답이 비어있음"),
    PDF_TOO_LARGE(HttpStatus.INTERNAL_SERVER_ERROR, "PDF의 크기가 업로드 제한 크기를 넘습니다."),
    PDF_DOWNLOAD_ERROR(HttpStatus.BAD_GATEWAY, "PDF 다운로드 실패"),
    ;

    private final HttpStatus status;
    private final String message;
}
