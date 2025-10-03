package starlight.adapter.spellcheck.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import starlight.adapter.spellcheck.webapi.dto.SpellCheckRequest;
import starlight.adapter.spellcheck.webapi.dto.SpellCheckResponse;
import starlight.shared.apiPayload.response.ApiResponse;

@Tag(name = "맞춤법 검사", description = "다음 맞춤법 검사 API")
public interface SpellCheckApiDoc {

    @Operation(
            summary = "맞춤법 검사 및 교정",
            description = "입력된 텍스트의 맞춤법을 검사하고 교정된 텍스트를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "검사 성공",
                    content = @Content(
                            schema = @Schema(implementation = SpellCheckResponse.class),
                            examples = @ExampleObject(
                                    name = "맞춤법 오류 발견",
                                    value = """
                                            {
                                              "result": "SUCCESS",
                                              "data": {
                                                "typos": [
                                                  {
                                                    "type": "space",
                                                    "severity": "normal",
                                                    "token": "반갑습니다다리",
                                                    "suggestions": [
                                                      "반갑습니다 다리"
                                                    ],
                                                    "visible": "반갑습니다 다리",
                                                    "original": "반갑습니다다리",
                                                    "context": "안녕하세요 반갑습니다다리",
                                                    "help": "띄어쓰기 오류입니다. 대치어를 참고하여 고쳐 쓰세요.",
                                                    "examples": []
                                                  }
                                                ],
                                                "corrected": "안녕하세요 반갑습니다 다리"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "빈 텍스트",
                                    value = """
                                            {
                                              "result": "ERROR",
                                              "data": null,
                                              "error": "텍스트를 입력해주세요"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/daum/check")
    ApiResponse<SpellCheckResponse> check(
            @RequestBody(
                    description = "검사할 텍스트",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "예시 요청",
                                    value = """
                                            {
                                              "text": "안녕하세요 반갑습니다다리"
                                            }
                                            """
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody
            SpellCheckRequest request
    );
}