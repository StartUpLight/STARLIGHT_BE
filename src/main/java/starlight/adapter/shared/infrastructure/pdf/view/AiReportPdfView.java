package starlight.adapter.shared.infrastructure.pdf.view;

import java.util.List;

public record AiReportPdfView(
        int totalScore,
        List<SectionView> sections,
        List<StrengthWeakness> strengths,
        List<StrengthWeakness> weaknesses,
        String radarChartSvg
) {
    public record SectionView(
            String title,
            int score,
            int total,
            List<ChecklistItem> checklist
    ) {
    }

    public record ChecklistItem(
            String item,
            Integer score,
            Integer maxScore
    ) {
    }

    public record StrengthWeakness(
            String title,
            String content
    ) {
    }
}
