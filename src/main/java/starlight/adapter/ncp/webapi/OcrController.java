package starlight.adapter.ncp.webapi;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import starlight.adapter.ncp.ocr.ClovaOcrProvider;
import starlight.shared.dto.ClovaOcrResponse;
import starlight.shared.apiPayload.response.ApiResponse;

@RestController
@RequestMapping("/v1/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final ClovaOcrProvider service;

    @PostMapping("/pdf")
    public ApiResponse<ClovaOcrResponse> ocrPdf(@RequestParam String url) {
        return ApiResponse.success(service.ocrPdfByUrl(url));
    }

    @PostMapping("/pdf/text")
    public ApiResponse<String> ocrPdfText(@RequestParam String url) {
        return ApiResponse.success(service.ocrPdfTextByUrl(url));
    }
}

