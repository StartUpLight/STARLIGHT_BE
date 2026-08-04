package starlight.adapter.shared.infrastructure.pdf.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import starlight.adapter.shared.infrastructure.pdf.view.AiReportPdfView;
import starlight.application.aireport.provided.dto.AiReportResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AiReportPdfViewMapper {

    private static final List<SectionMeta> SECTION_ORDER = List.of(
            new SectionMeta("PROBLEM_RECOGNITION", "문제 정의", "problemRecognitionScore", 20),
            new SectionMeta("FEASIBILITY", "실현 가능성", "feasibilityScore", 30),
            new SectionMeta("GROWTH_STRATEGY", "성장 전략", "growthStrategyScore", 30),
            new SectionMeta("TEAM_COMPETENCE", "팀 역량", "teamCompetenceScore", 20)
    );

    private final ObjectMapper objectMapper;

    public AiReportPdfView toView(AiReportResult aiReportResult) {
        List<AiReportPdfView.SectionView> sections = buildSections(aiReportResult);
        int problemScore = nullable(aiReportResult.problemRecognitionScore());
        int feasibilityScore = nullable(aiReportResult.feasibilityScore());
        int growthScore = nullable(aiReportResult.growthStrategyScore());
        int teamScore = nullable(aiReportResult.teamCompetenceScore());
        return new AiReportPdfView(
                nullable(aiReportResult.totalScore()),
                sections,
                mapStrengthWeakness(aiReportResult.strengths()),
                mapStrengthWeakness(aiReportResult.weaknesses()),
                buildRadarSvg(problemScore, feasibilityScore, growthScore, teamScore)
        );
    }

    private String buildRadarSvg(int problemScore, int feasibilityScore, int growthScore, int teamScore) {
        int cx = 180;
        int cy = 110;
        int radius = 72;
        double[] ratios = {
                problemScore / 20.0,
                feasibilityScore / 30.0,
                growthScore / 30.0,
                teamScore / 20.0
        };
        String polygon = String.format(
                "%d,%d %d,%d %d,%d %d,%d",
                cx, (int) (cy - radius * ratios[0]),
                (int) (cx + radius * ratios[1]), cy,
                cx, (int) (cy + radius * ratios[2]),
                (int) (cx - radius * ratios[3]), cy
        );

        StringBuilder rings = new StringBuilder();
        int[][] ringDefs = {{18, 1}, {36, 0}, {54, 1}, {72, 0}};
        for (int[] ring : ringDefs) {
            int r = ring[0];
            String dash = ring[1] == 1 ? " stroke-dasharray=\"4 4\"" : "";
            rings.append(String.format(
                    "<circle cx=\"%d\" cy=\"%d\" r=\"%d\" fill=\"#F3F5F9\" stroke=\"#DADFE7\" stroke-width=\"1.2\"%s />",
                    cx, cy, r, dash
            ));
        }

        return """
                <svg width="360" height="220" viewBox="0 0 360 220" xmlns="http://www.w3.org/2000/svg">
                  %s
                  <line x1="%d" y1="%d" x2="%d" y2="%d" stroke="#EBEEF3" stroke-width="1.2" />
                  <line x1="%d" y1="%d" x2="%d" y2="%d" stroke="#EBEEF3" stroke-width="1.2" />
                  <polygon points="%s" fill="#E8E2FF" stroke="#6F55FF" stroke-width="1.2" />
                  <text x="%d" y="%d" text-anchor="middle" fill="#191F28" font-size="10" font-weight="500" font-family="PdfReportFont, Malgun Gothic, sans-serif">문제 정의</text>
                  <text x="%d" y="%d" text-anchor="middle" fill="#191F28" font-size="10" font-weight="500" font-family="PdfReportFont, Malgun Gothic, sans-serif">실현 가능성</text>
                  <text x="%d" y="%d" text-anchor="middle" fill="#191F28" font-size="10" font-weight="500" font-family="PdfReportFont, Malgun Gothic, sans-serif">성장 전략</text>
                  <text x="%d" y="%d" text-anchor="middle" fill="#191F28" font-size="10" font-weight="500" font-family="PdfReportFont, Malgun Gothic, sans-serif">팀 역량</text>
                </svg>
                """.formatted(
                rings,
                cx, cy - radius, cx, cy + radius,
                cx - radius, cy, cx + radius, cy,
                polygon,
                cx, cy - radius - 16,
                cx + radius + 40, cy + 4,
                cx, cy + radius + 20,
                cx - radius - 40, cy + 4
        ).trim();
    }

    private List<AiReportPdfView.SectionView> buildSections(AiReportResult aiReportResult) {
        Map<String, AiReportResult.SectionScoreDetailResponse> sectionScoreMap =
                (aiReportResult.sectionScores() == null ? List.<AiReportResult.SectionScoreDetailResponse>of() : aiReportResult.sectionScores())
                        .stream()
                        .collect(Collectors.toMap(
                                AiReportResult.SectionScoreDetailResponse::sectionType,
                                section -> section,
                                (left, right) -> left
                        ));

        return SECTION_ORDER.stream()
                .map(meta -> {
                    AiReportResult.SectionScoreDetailResponse section = sectionScoreMap.get(meta.key());
                    List<AiReportPdfView.ChecklistItem> checklist = parseChecklist(
                            section == null ? "[]" : section.gradingListScores()
                    );
                    int score = switch (meta.scoreKey()) {
                        case "problemRecognitionScore" -> nullable(aiReportResult.problemRecognitionScore());
                        case "feasibilityScore" -> nullable(aiReportResult.feasibilityScore());
                        case "growthStrategyScore" -> nullable(aiReportResult.growthStrategyScore());
                        case "teamCompetenceScore" -> nullable(aiReportResult.teamCompetenceScore());
                        default -> 0;
                    };
                    return new AiReportPdfView.SectionView(meta.title(), score, meta.total(), checklist);
                })
                .toList();
    }

    private List<AiReportPdfView.ChecklistItem> parseChecklist(String gradingListScores) {
        try {
            List<AiReportPdfView.ChecklistItem> list = objectMapper.readValue(
                    gradingListScores == null ? "[]" : gradingListScores,
                    new TypeReference<>() {
                    });
            return list.stream()
                    .filter(item -> item != null && item.item() != null)
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<AiReportPdfView.StrengthWeakness> mapStrengthWeakness(List<AiReportResult.StrengthWeakness> source) {
        if (source == null) {
            return List.of();
        }
        return source.stream()
                .map(item -> new AiReportPdfView.StrengthWeakness(item.title(), item.content()))
                .toList();
    }

    private static int nullable(Integer score) {
        return score == null ? 0 : score;
    }

    private record SectionMeta(String key, String title, String scoreKey, int total) {
    }
}
