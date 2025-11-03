package starlight.application.expertApplication.required;

import starlight.application.expertApplication.required.dto.FeedbackRequestEmailDto;

public interface EmailSender {

    void sendFeedbackRequestMail(FeedbackRequestEmailDto dto);
}
