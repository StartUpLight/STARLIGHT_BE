package starlight.application.expertApplication.provided;

public interface ExpertApplicationCommandUseCase {

    void requestFeedback(Long expertId, Long planId, String pdfUrl, String menteeName);
}
