package starlight.application.expertApplication.required;

import starlight.application.expertApplication.event.FeedbackRequestInput;

public interface FeedbackRequestMailPort {

    void sendFeedbackRequestMail(FeedbackRequestInput dto);
}
