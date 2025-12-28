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
    @DisplayName("Comments 업데이트 - null 예외")
    void updateComments_Null_ThrowsException() {
        // given
        ExpertReport report = ExpertReport.create(1L, 10L, "token");

        // when & then
        assertThatThrownBy(() -> report.updateComments(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("comments는 null일 수 없습니다");
    }

    @Test
    @DisplayName("Comments 업데이트 - 성공")
    void updateComments_Success() {
        // given
        ExpertReport report = ExpertReport.create(1L, 10L, "token");
        List<ExpertReportComment> comments = List.of(
                ExpertReportComment.create(CommentType.STRENGTH, "좋습니다"),
                ExpertReportComment.create(CommentType.WEAKNESS, "개선 필요")
        );

        // when
        report.updateComments(comments);

        // then
        assertThat(report.getComments()).hasSize(2);
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
    @DisplayName("ExpertReportComment 생성 - 성공")
    void createComment_Success() {
        // given
        CommentType type = CommentType.STRENGTH;
        String content = "시장 분석이 우수합니다.";

        // when
        ExpertReportComment comment = ExpertReportComment.create(type, content);

        // then
        assertThat(comment).isNotNull();
        assertThat(comment.getType()).isEqualTo(type);
        assertThat(comment.getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("ExpertReportComment 생성 - content empty 예외")
    void createComment_EmptyContent_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
                ExpertReportComment.create(CommentType.STRENGTH, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("content는 필수입니다");
    }
}
