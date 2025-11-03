package starlight.application.expertApplication.event;

import lombok.Builder;
import lombok.Getter;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.expert.entity.Expert;

/**
 * 피드백 요청 이메일 전송 이벤트
 * 트랜잭션 커밋 후 비동기로 처리됨
 */
@Getter
@Builder
public class FeedbackRequestEvent {
    private final Expert expert;
    private final BusinessPlan plan;
    private final String menteeName;
    private final byte[] fileBytes;
    private final String filename;
    private final String feedbackUrl;
}