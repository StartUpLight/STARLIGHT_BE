package starlight.application.expertApplication.provided;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ExpertApplicationServiceUseCase {

    void requestFeedback(Long expertId, Long planId, MultipartFile file, String menteeName) throws IOException;
}
