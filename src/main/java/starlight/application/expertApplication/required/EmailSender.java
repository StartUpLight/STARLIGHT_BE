package starlight.application.expertApplication.required;

import starlight.application.expertApplication.event.FeedbackRequestInput;

public interface EmailSender {

    void sendFeedbackRequestMail(FeedbackRequestInput dto);
}
