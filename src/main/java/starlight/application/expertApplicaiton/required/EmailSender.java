package starlight.application.expertApplicaiton.required;

import starlight.application.expertApplicaiton.required.dto.FeedbackRequestEmailDto;

public interface EmailSender {

    void sendFeedbackRequestMail(FeedbackRequestEmailDto dto);
}
