package starlight.adapter.ai.util;

import org.springframework.stereotype.Component;
import starlight.domain.businessplan.entity.BaseSection;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.SubSection;
import starlight.domain.businessplan.enumerate.SubSectionType;
import starlight.shared.enumerate.SectionType;

import java.util.ArrayList;
import java.util.List;

/**
 * BusinessPlan에서 LLM 채점을 위한 텍스트 컨텐츠를 추출하는 컴포넌트
 */
@Component
public class BusinessPlanContentExtractor {

    /**
     * BusinessPlan에서 모든 섹션의 컨텐츠를 추출하여 하나의 문자열로 반환
     */
    public String extractContent(BusinessPlan businessPlan) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("다음 사업계획서 내용을 채점해주세요:\n\n");

        List<String> sections = new ArrayList<>();

        // 문제 인식 섹션
        String problemRecognition = extractSectionContent(
                businessPlan.getProblemRecognition(),
                SectionType.PROBLEM_RECOGNITION,
                "문제 인식");
        if (!problemRecognition.isBlank()) {
            sections.add(problemRecognition);
        }

        // 실현 가능성 섹션
        String feasibility = extractSectionContent(
                businessPlan.getFeasibility(),
                SectionType.FEASIBILITY,
                "실현 가능성");
        if (!feasibility.isBlank()) {
            sections.add(feasibility);
        }

        // 성장 전략 섹션
        String growthStrategy = extractSectionContent(
                businessPlan.getGrowthTactic(),
                SectionType.GROWTH_STRATEGY,
                "성장 전략");
        if (!growthStrategy.isBlank()) {
            sections.add(growthStrategy);
        }

        // 팀 역량 섹션
        String teamCompetence = extractSectionContent(
                businessPlan.getTeamCompetence(),
                SectionType.TEAM_COMPETENCE,
                "팀 역량");
        if (!teamCompetence.isBlank()) {
            sections.add(teamCompetence);
        }

        promptBuilder.append(String.join("\n\n", sections));
        return promptBuilder.toString();
    }

    /**
     * 특정 섹션의 컨텐츠를 추출
     */
    private String extractSectionContent(BaseSection section, SectionType sectionType, String sectionTitle) {
        if (section == null) {
            return "";
        }

        StringBuilder sectionBuilder = new StringBuilder();
        sectionBuilder.append("## ").append(sectionTitle).append("\n");

        for (SubSectionType subSectionType : SubSectionType.values()) {
            if (subSectionType.getSectionType() == sectionType) {
                SubSection subSection = section.getSubSectionByType(subSectionType);
                if (subSection != null && subSection.getContent() != null && !subSection.getContent().isBlank()) {
                    sectionBuilder.append("### ").append(subSectionType.getDescription()).append("\n");
                    sectionBuilder.append(subSection.getContent()).append("\n");
                }
            }
        }

        return sectionBuilder.toString();
    }
}

