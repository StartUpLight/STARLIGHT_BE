package starlight.application.expertApplication.required;

import starlight.application.expertApplication.event.FeedbackRequestDto;

public interface EmailSender {

    void sendFeedbackRequestMail(FeedbackRequestDto dto);
}
