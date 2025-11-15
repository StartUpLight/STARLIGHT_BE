package starlight.payment.toss.domain.enumerate;

public enum PaymentState {
    REQUESTED,     // 결제요청(미승인)
    DONE,          // 승인완료
    CANCELED,      // 전체취소
    PARTIAL_CANCELED, // 부분취소(향후 필요시)
    FAILED         // 승인 실패(옵션)
}