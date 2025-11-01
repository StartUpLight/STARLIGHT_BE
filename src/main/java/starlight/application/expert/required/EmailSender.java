package starlight.application.expert.required;

public interface EmailSender {

    void sendFeedbackRequestMail(FeedbackRequestEmailDto dto);
}
