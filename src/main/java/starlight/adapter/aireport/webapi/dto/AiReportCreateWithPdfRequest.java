package starlight.adapter.aireport.webapi.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record AiReportCreateWithPdfRequest(
    @NotBlank(message = "제목은 필수입니다.")
    String title,

    // TODO: 버킷 정책 등에 따라서 이후에 host 강제할 것
    @NotBlank(message = "PDF URL은 필수입니다.")
    @URL(protocol = "https", message = "https URL만 허용됩니다.")
    String pdfUrl
) {}
