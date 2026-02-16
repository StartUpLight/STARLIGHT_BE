package starlight.application.businessplan.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import starlight.domain.businessplan.entity.BaseSection;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.SubSection;
import starlight.domain.businessplan.enumerate.SubSectionType;
import starlight.shared.enumerate.SectionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BusinessPlan에서 LLM 채점을 위한 텍스트 컨텐츠를 추출하는 컴포넌트
 */
@Slf4j
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

    /**
     * BusinessPlan에서 섹션별로 컨텐츠를 추출하여 Map으로 반환
     */
    public Map<SectionType, String> extractSectionContents(BusinessPlan businessPlan) {
        Map<SectionType, String> sectionContents = new HashMap<>();
        
        String problemRecognition = extractSectionContent(
            businessPlan.getProblemRecognition(),
            SectionType.PROBLEM_RECOGNITION, "문제 인식");
        sectionContents.put(SectionType.PROBLEM_RECOGNITION, problemRecognition);
        
        String feasibility = extractSectionContent(
            businessPlan.getFeasibility(),
            SectionType.FEASIBILITY, "실현 가능성");
        sectionContents.put(SectionType.FEASIBILITY, feasibility);
        
        String growthStrategy = extractSectionContent(
            businessPlan.getGrowthTactic(),
            SectionType.GROWTH_STRATEGY, "성장 전략");
        sectionContents.put(SectionType.GROWTH_STRATEGY, growthStrategy);
        
        String teamCompetence = extractSectionContent(
            businessPlan.getTeamCompetence(),
            SectionType.TEAM_COMPETENCE, "팀 역량");
        sectionContents.put(SectionType.TEAM_COMPETENCE, teamCompetence);
        
        return sectionContents;
    }

    /**
     * 전체 텍스트에서 섹션별로 내용을 추출 (PDF 케이스용)
     * 
     * 현재 PDF 입력은 섹션별 채점 대신 FullReportGradeAgent를 사용 중
     * 현재 PDF 처리는 {@link starlight.application.aireport.required.ReportGraderPort#gradeWithFullPrompt(String)}를 사용
     * 
     * @param fullContent 전체 텍스트 내용
     * @return 섹션별 내용 맵 (현재는 모든 섹션에 전체 내용을 할당)
     * TODO: 실제 구현 필요 - 섹션 제목을 기준으로 파싱
     */
//    public Map<SectionType, String> extractSectionContentsFromText(String fullContent) {
//        // 간단한 구현: 전체 내용을 각 섹션에 동일하게 할당
//        // 나중에 실제 파싱 로직으로 개선 필요
//        Map<SectionType, String> sectionContents = new HashMap<>();
//
//        // 섹션 제목을 찾아서 분리하는 로직 필요
//        // 현재는 전체 내용을 각 섹션에 할당
//        sectionContents.put(SectionType.PROBLEM_RECOGNITION, fullContent);
//        sectionContents.put(SectionType.FEASIBILITY, fullContent);
//        sectionContents.put(SectionType.GROWTH_STRATEGY, fullContent);
//        sectionContents.put(SectionType.TEAM_COMPETENCE, fullContent);
//
//        return sectionContents;
//    }
}

