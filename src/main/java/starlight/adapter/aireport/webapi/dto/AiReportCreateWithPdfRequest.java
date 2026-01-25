package starlight.adapter.aireport.webapi.dto;

import jakarta.validation.constraints.NotBlank;

public record AiReportCreateWithPdfRequest(
    @NotBlank(message = "제목은 필수입니다.")
    String title,

    @NotBlank(message = "PDF URL은 필수입니다.")
    String pdfUrl
) {}
