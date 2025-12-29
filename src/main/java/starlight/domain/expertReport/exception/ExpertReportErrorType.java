package starlight.domain.expertReport.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import starlight.shared.apiPayload.exception.ErrorType;

@Getter
@RequiredArgsConstructor
public enum ExpertReportErrorType implements ErrorType {

    // 전문가 피드백 신청 관련 오류
    EXPERT_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 전문가 리포트를 찾을 수 없습니다."),
    ALREADY_SUBMITTED(HttpStatus.BAD_REQUEST, "이미 전문가 피드백을 제출하였습니다."),
    REPORT_EXPIRED(HttpStatus.BAD_REQUEST, "전문가 피드백 요청 기간이 만료되었습니다."),
    ;

    private final HttpStatus status;

    private final String message;
}
