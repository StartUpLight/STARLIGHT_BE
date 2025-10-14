package starlight.adapter.ncp.webapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import starlight.adapter.auth.security.filter.JwtFilter;
import starlight.application.infrastructure.dto.PreSignedUrlResponse;
import starlight.application.infrastructure.provided.PresignedUrlProvider;
import starlight.bootstrap.SecurityConfig;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ImageController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        JwtFilter.class,
                        SecurityConfig.class
                })
        }
)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ImageController 통합 테스트")
class ImageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PresignedUrlProvider presignedUrlProvider;

    @MockitoBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("GET /v1/image/upload - Presigned URL 조회 성공")
    void getPresignedUrl_Success() throws Exception {
        // given
        Long userId = 1L;
        String fileName = "test-image.jpg";
        String preSignedUrl = "https://test-bucket.kr.object.ncloudstorage.com/presigned-url";
        String objectUrl = "https://test-bucket.kr.object.ncloudstorage.com/1/test-image.jpg";

        PreSignedUrlResponse response = PreSignedUrlResponse.of(preSignedUrl, objectUrl);
        given(presignedUrlProvider.getPreSignedUrl(userId, fileName)).willReturn(response);

        // when & then
        mockMvc.perform(get("/v1/image/upload")
                        .param("userId", userId.toString())
                        .param("fileName", fileName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.preSignedUrl").value(preSignedUrl))
                .andExpect(jsonPath("$.data.objectUrl").value(objectUrl));

        verify(presignedUrlProvider).getPreSignedUrl(userId, fileName);
    }

    @Test
    @DisplayName("GET /v1/image/upload - userId 누락 시 400 에러")
    void getPresignedUrl_MissingUserId() throws Exception {
        // when & then
        mockMvc.perform(get("/v1/image/upload")
                        .param("fileName", "test.jpg")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(presignedUrlProvider, never()).getPreSignedUrl(any(), any());
    }

    @Test
    @DisplayName("GET /v1/image/upload - fileName 누락 시 400 에러")
    void getPresignedUrl_MissingFileName() throws Exception {
        // when & then
        mockMvc.perform(get("/v1/image/upload")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(presignedUrlProvider, never()).getPreSignedUrl(any(), any());
    }

    @Test
    @DisplayName("POST /v1/image/upload/public - 이미지 공개 처리 성공")
    void finalizePublic_Success() throws Exception {
        // given
        String objectUrl = "https://test-bucket.kr.object.ncloudstorage.com/1/test-image.jpg";
        given(presignedUrlProvider.makePublic(objectUrl)).willReturn(objectUrl);

        // when & then
        mockMvc.perform(post("/v1/image/upload/public")
                        .param("objectUrl", objectUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(objectUrl));

        verify(presignedUrlProvider).makePublic(objectUrl);
    }

    @Test
    @DisplayName("POST /v1/image/upload/public - objectUrl 누락 시 400 에러")
    void finalizePublic_MissingObjectUrl() throws Exception {
        // when & then
        mockMvc.perform(post("/v1/image/upload/public")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(presignedUrlProvider, never()).makePublic(any());
    }

    @Test
    @DisplayName("POST /v1/image/upload/public - 잘못된 URL 형식으로 예외 발생")
    void finalizePublic_InvalidUrl() throws Exception {
        // given
        String invalidUrl = "invalid-url";
        given(presignedUrlProvider.makePublic(invalidUrl))
                .willThrow(new IllegalArgumentException("잘못된 URL 형식"));

        // when & then
        mockMvc.perform(post("/v1/image/upload/public")
                        .param("objectUrl", invalidUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        verify(presignedUrlProvider).makePublic(invalidUrl);
    }
}