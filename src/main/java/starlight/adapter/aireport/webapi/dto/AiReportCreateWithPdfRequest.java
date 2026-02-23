package starlight.adapter.aireport.webapi.dto;

import jakarta.validation.constraints.NotBlank;
import starlight.adapter.shared.webapi.validation.ValidPdfUrl;

public record AiReportCreateWithPdfRequest(
    @NotBlank(message = "제목은 필수입니다.")
    String title,

    @NotBlank(message = "PDF URL은 필수입니다.")
    @ValidPdfUrl
    String pdfUrl
) {}
