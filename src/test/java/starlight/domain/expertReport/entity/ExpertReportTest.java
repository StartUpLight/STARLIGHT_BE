package starlight.domain.expertReport.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starlight.domain.expertReport.enumerate.CommentType;
import starlight.domain.expertReport.enumerate.SubmitStatus;
import starlight.domain.expertReport.exception.ExpertReportException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ExpertReportTest {

    @Test
    @DisplayName("ExpertReport 생성 - 성공")
    void createExpertReport_Success() {
        // given
        Long expertId = 1L;
        Long businessPlanId = 10L;
        String token = "abc123";
        LocalDateTime beforeCreation = LocalDateTime.now();

        // when
        ExpertReport report = ExpertReport.create(expertId, businessPlanId, token);

        // then
        assertThat(report).isNotNull();
        assertThat(report.getExpertId()).isEqualTo(expertId);
        assertThat(report.getBusinessPlanId()).isEqualTo(businessPlanId);
        assertThat(report.getToken()).isEqualTo(token);
        assertThat(report.getSubmitStatus()).isEqualTo(SubmitStatus.PENDING);
        assertThat(report.getViewCount()).isEqualTo(0);
        assertThat(report.getExpiredAt()).isAfter(beforeCreation);
    }

    @Test
    @DisplayName("ExpertReport 생성 - expertId null 예외")
    void createExpertReport_NullExpertId_ThrowsException() {
        // given
        Long expertId = null;
        Long businessPlanId = 10L;
        String token = "abc123";

        // when & then
        assertThatThrownBy(() -> ExpertReport.create(expertId, businessPlanId, token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expertId는 필수입니다");
    }

    @Test
    @DisplayName("ExpertReport 생성 - token empty 예외")
    void createExpertReport_EmptyToken_ThrowsException() {
        // given
        Long expertId = 1L;
        Long businessPlanId = 10L;
        String token = "";

        // when & then
        assertThatThrownBy(() -> ExpertReport.create(expertId, businessPlanId, token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("token은 필수입니다");
    }

    @Test
    @DisplayName("임시 저장 - 성공")
    void temporarySave_Success() {
        // given
        ExpertReport report = ExpertReport.create(1L, 10L, "token");

        // when
        report.temporarySave();

        // then
        assertThat(report.getSubmitStatus()).isEqualTo(SubmitStatus.TEMPORARY_SAVED);
        assertThat(report.canEdit()).isTrue();
    }

    @Test
    @DisplayName("최종 제출 - 성공")
    void submit_Success() {
        // given
        ExpertReport report = ExpertReport.create(1L, 10L, "token");

        // when
        report.submit();

        // then
        assertThat(report.getSubmitStatus()).isEqualTo(SubmitStatus.SUBMITTED);
        assertThat(report.canEdit()).isFalse();
    }

    @Test
    @DisplayName("제출 후 재편집 시도 - 예외")
    void editAfterSubmit_ThrowsException() {
        // given
        ExpertReport report = ExpertReport.create(1L, 10L, "token");
        report.submit();

        // when & then
        assertThatThrownBy(() -> report.updateOverallComment("새로운 의견"))
                .isInstanceOf(ExpertReportException.class);
    }

    @Test
    @DisplayName("Details 업데이트 - null 예외")
    void updateDetails_Null_ThrowsException() {
        // given
        ExpertReport report = ExpertReport.create(1L, 10L, "token");

        // when & then
        assertThatThrownBy(() -> report.updateDetails(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("details는 null일 수 없습니다");
    }

    @Test
    @DisplayName("Details 업데이트 - 성공")
    void updateDetails_Success() {
        // given
        ExpertReport report = ExpertReport.create(1L, 10L, "token");
        List<ExpertReportDetail> details = List.of(
                ExpertReportDetail.create(CommentType.STRENGTH, "강점", "좋습니다"),
                ExpertReportDetail.create(CommentType.WEAKNESS, "약점", "개선 필요")
        );

        // when
        report.updateDetails(details);

        // then
        assertThat(report.getDetails()).hasSize(2);
    }

    @Test
    @DisplayName("조회수 증가")
    void incrementViewCount_Success() {
        // given
        ExpertReport report = ExpertReport.create(1L, 10L, "token");
        int initialCount = report.getViewCount();

        // when
        report.incrementViewCount();
        report.incrementViewCount();

        // then
        assertThat(report.getViewCount()).isEqualTo(initialCount + 2);
    }

    @Test
    @DisplayName("ExpertReportDetail 생성 - 성공")
    void createDetail_Success() {
        // given
        CommentType type = CommentType.STRENGTH;
        String title = "강점 분석";
        String content = "시장 분석이 우수합니다.";

        // when
        ExpertReportDetail detail = ExpertReportDetail.create(type, title, content);

        // then
        assertThat(detail).isNotNull();
        assertThat(detail.getCommentType()).isEqualTo(type);
        assertThat(detail.getTitle()).isEqualTo(title);
        assertThat(detail.getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("ExpertReportDetail 생성 - content empty 예외")
    void createDetail_EmptyContent_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
                ExpertReportDetail.create(CommentType.STRENGTH, "title", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("content는 필수입니다");
    }
}