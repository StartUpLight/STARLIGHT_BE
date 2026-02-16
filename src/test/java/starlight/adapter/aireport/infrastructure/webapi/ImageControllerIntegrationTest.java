package starlight.adapter.aireport.infrastructure.webapi;

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
import starlight.adapter.aireport.webapi.ImageController;
import starlight.adapter.member.auth.security.auth.AuthDetails;
import starlight.adapter.member.auth.security.filter.JwtFilter;
import starlight.application.aireport.required.PresignedUrlProviderPort;
import starlight.bootstrap.SecurityConfig;
import starlight.domain.member.entity.Member;
import starlight.domain.member.enumerate.MemberType;

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
    private PresignedUrlProviderPort presignedUrlProvider;

    @MockitoBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private AuthDetails createMockAuthDetails(Long memberId) {
        Member mockMember = mock(Member.class);
        given(mockMember.getId()).willReturn(memberId);
        given(mockMember.getEmail()).willReturn("test@example.com");
        given(mockMember.getMemberType()).willReturn(MemberType.FOUNDER);
        return new AuthDetails(mockMember);
    }

//    @Test
//    @DisplayName("GET /v1/images/upload-url - Presigned URL 조회 성공")
//    @WithMockUser // (선택) user(...)와 중복이면 제거 가능
//    void getPresignedUrl_Success() throws Exception {
//        // given
//        Long userId = 1L;
//        String fileName = "test-image.jpg";
//        String preSignedUrl = "https://test-bucket.kr.object.ncloudstorage.com/presigned-url";
//        String objectUrl    = "https://test-bucket.kr.object.ncloudstorage.com/1/test-image.jpg";
//
//        PreSignedUrlResponse response = PreSignedUrlResponse.of(preSignedUrl, objectUrl);
//        given(presignedUrlProvider.getPreSignedUrl(userId, fileName)).willReturn(response);
//
//        // when & then
//        mockMvc.perform(get("/v1/images/upload-url")
//                        .with(user(createMockAuthDetails(userId)))
//                        .param("fileName", fileName)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.result").value("SUCCESS"))
//                .andExpect(jsonPath("$.data.preSignedUrl").value(preSignedUrl))
//                .andExpect(jsonPath("$.data.objectUrl").value(objectUrl));
//
//        verify(presignedUrlProvider).getPreSignedUrl(userId, fileName);
//    }

    @Test
    @DisplayName("GET /v1/images/upload-url - fileName 누락 시 400 에러")
    void getPresignedUrl_MissingFileName() throws Exception {
        // when & then
        mockMvc.perform(get("/v1/images/upload-url")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(presignedUrlProvider, never()).getPreSignedUrl(any(), any());
    }

    @Test
    @DisplayName("POST /v1/images/upload-url/public - 이미지 공개 처리 성공")
    void finalizePublic_Success() throws Exception {
        // given
        String objectUrl = "https://test-bucket.kr.object.ncloudstorage.com/1/test-image.jpg";
        given(presignedUrlProvider.makePublic(objectUrl)).willReturn(objectUrl);

        // when & then
        mockMvc.perform(post("/v1/images/upload-url/public")
                        .param("objectUrl", objectUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(objectUrl));

        verify(presignedUrlProvider).makePublic(objectUrl);
    }

    @Test
    @DisplayName("POST /v1/images/upload-url/public - objectUrl 누락 시 400 에러")
    void finalizePublic_MissingObjectUrl() throws Exception {
        // when & then
        mockMvc.perform(post("/v1/images/upload-url/public")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(presignedUrlProvider, never()).makePublic(any());
    }

    @Test
    @DisplayName("POST /v1/images/upload-url/public - 잘못된 URL 형식으로 예외 발생")
    void finalizePublic_InvalidUrl() throws Exception {
        // given
        String invalidUrl = "invalid-url";
        given(presignedUrlProvider.makePublic(invalidUrl))
                .willThrow(new IllegalArgumentException("잘못된 URL 형식"));

        // when & then
        mockMvc.perform(post("/v1/images/upload-url/public")
                        .param("objectUrl", invalidUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        verify(presignedUrlProvider).makePublic(invalidUrl);
    }
}