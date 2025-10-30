package starlight.application.businessplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.adapter.businessplan.webapi.dto.SubSectionResponse;
import starlight.application.businessplan.provided.SubSectionService;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.application.businessplan.required.SubSectionQuery;
import starlight.application.businessplan.util.PlainTextExtractUtils;
import starlight.application.businessplan.required.ChecklistGrader;
import starlight.application.businessplan.util.SubSectionSupportUtils;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.SubSection;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.domain.businessplan.enumerate.SubSectionName;
import starlight.domain.businessplan.exception.BusinessPlanErrorType;
import starlight.domain.businessplan.exception.BusinessPlanException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubSectionServiceImpl implements SubSectionService {

    private final ObjectMapper objectMapper;
    private final SubSectionQuery subSectionQuery;
    private final BusinessPlanQuery businessPlanQuery;
    private final ChecklistGrader checklistGrader;

    @Override
    public SubSectionResponse.Created createOrUpdateSection(
            Long planId, JsonNode jsonNode, SubSectionName subSectionName
    ) {
        BusinessPlan businessPlan = businessPlanQuery.getOrThrow(planId);

        String rawJsonStr = SubSectionSupportUtils.serializeJsonNodeSafely(objectMapper, jsonNode);
        String content = PlainTextExtractUtils.extractPlainText(objectMapper, jsonNode);

        // 기존 서브섹션이 있는지 확인
        SubSection subSection = subSectionQuery.findByBusinessPlanIdAndSubSectionName(planId, subSectionName)
                .orElse(null);

        String responseMessage;
        if (subSection == null) {
            // 새로 생성
            subSection = SubSection.create(subSectionName, content, rawJsonStr);

            // SubSectionName의 SectionName을 통해 BusinessPlan에서 해당 섹션 조회 후 양방향 매핑
            attachSubSectionToParent(subSection, businessPlan);

            responseMessage = "created";
        } else {
            // 기존 것 업데이트
            subSection.updateContent(content, rawJsonStr);
            responseMessage = "updated";
        }

        SubSection savedSubSection = subSectionQuery.save(subSection);
        return SubSectionResponse.Created.create(subSectionName, savedSubSection.getId(), responseMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public SubSectionResponse.Retrieved getSubSection(Long planId, SubSectionName subSectionName) {
        SubSection subSection = subSectionQuery.findByBusinessPlanIdAndSubSectionName(planId, subSectionName)
                .orElseThrow(() -> new BusinessPlanException(BusinessPlanErrorType.SUBSECTION_NOT_FOUND));

        return SubSectionResponse.Retrieved.create("retrieved", subSection.getRawJson().asTree());
    }

    @Override
    public SubSectionResponse.Deleted deleteSubSection(Long planId, SubSectionName subSectionName) {
        SubSection subSection = subSectionQuery.findByBusinessPlanIdAndSubSectionName(planId, subSectionName)
                .orElseThrow(() -> new BusinessPlanException(BusinessPlanErrorType.SUBSECTION_NOT_FOUND));

        subSectionQuery.delete(subSection);

        return SubSectionResponse.Deleted.create(subSectionName, subSection.getId(), "deleted");
    }

    @Override
    public List<Boolean> checkSubSection(
            Long planId, JsonNode jsonNode, SubSectionName subSectionName
    ) {
        SubSection subSection = subSectionQuery.findByBusinessPlanIdAndSubSectionName(planId, subSectionName)
                .orElseThrow(() -> new BusinessPlanException(BusinessPlanErrorType.SUBSECTION_NOT_FOUND));

        String content = PlainTextExtractUtils.extractPlainText(objectMapper, jsonNode);

        // RAG 기반 서브섹션별 체크리스트 수행
        List<Boolean> checks = checklistGrader.check(
                subSectionName,
                content);

        SubSectionSupportUtils.requireSize(checks, SubSection.getCHECKLIST_SIZE());

        subSection.updateChecks(checks);

        subSectionQuery.save(subSection);

        return checks;
    }

    /**
     * 단일 parent 참조 방식으로 SubSection을 부모 섹션에 연결
     */
    private void attachSubSectionToParent(SubSection subSection, BusinessPlan businessPlan) {
        SectionName sectionName = subSection.getSubSectionName().getSectionName();
        Long parentSectionId;

        switch (sectionName) {
            case OVERVIEW -> parentSectionId = businessPlan.getOverview().getId();
            case PROBLEM_RECOGNITION -> parentSectionId = businessPlan.getProblemRecognition().getId();
            case FEASIBILITY -> parentSectionId = businessPlan.getFeasibility().getId();
            case GROWTH_STRATEGY -> parentSectionId = businessPlan.getGrowthTactic().getId();
            case TEAM_COMPETENCE -> parentSectionId = businessPlan.getTeamCompetence().getId();
            default -> throw new IllegalStateException("Unknown section name: " + sectionName);
        }

        subSection.attachToParent(parentSectionId, sectionName);
    }
}
