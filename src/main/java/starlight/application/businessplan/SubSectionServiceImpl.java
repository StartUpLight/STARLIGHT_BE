package starlight.application.businessplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.adapter.businessplan.persistence.SubSectionRepository;
import starlight.adapter.businessplan.webapi.dto.SubSectionRequest;
import starlight.adapter.businessplan.webapi.dto.SubSectionResponse;
import starlight.application.businessplan.provided.SubSectionService;
import starlight.application.businessplan.required.BusinessPlanQuery;
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
    private final SubSectionRepository subSectionRepository;
    private final BusinessPlanQuery businessPlanQuery;
    private final ChecklistGrader checklistGrader;

    @Override
    public SubSectionResponse.Created createOrUpdateSection(Long planId, @Valid SubSectionRequest request) {
        BusinessPlan businessPlan = businessPlanQuery.getOrThrow(planId);
        SubSectionName subSectionName = request.subSectionName();
        JsonNode rawJson = objectMapper.valueToTree(request);
        String rawJsonStr = SubSectionSupportUtils.serializeJsonNodeSafely(objectMapper, rawJson);

        String content = PlainTextExtractUtils.extractPlainText(objectMapper, request);

        // 기존 서브섹션이 있는지 확인
        SubSection subSection = subSectionRepository.findByBusinessPlanIdAndSubSectionName(planId, subSectionName)
                .orElse(null);

        String responseMessage;
        if (subSection == null) {
            // 새로 생성
            subSection = SubSection.create(subSectionName, content, rawJsonStr);

            // SubSectionName의 SectionName을 통해 BusinessPlan에서 해당 섹션 조회 후 양방향 매핑
            Object parentSection = getParentSectionBySubSectionName(businessPlan, subSectionName);
            subSection.attachToParentSection(parentSection);

            responseMessage = "created";
        } else {
            // 기존 것 업데이트
            subSection.updateContent(content, rawJsonStr);
            responseMessage = "updated";
        }

        SubSection savedSubSection = subSectionRepository.save(subSection);
        return SubSectionResponse.Created.create(subSectionName, savedSubSection.getId(), responseMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public SubSectionResponse.Retrieved getSubSection(Long planId, SubSectionName subSectionName) {
        SubSection subSection = subSectionRepository.findByBusinessPlanIdAndSubSectionName(planId, subSectionName)
                .orElseThrow(() -> new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_NOT_FOUND));

        return SubSectionResponse.Retrieved.create("retrieved", subSection.getRawJson().asTree());
    }

    @Override
    public SubSectionResponse.Deleted deleteSubSection(Long planId, SubSectionName subSectionName) {
        SubSection subSection = subSectionRepository.findByBusinessPlanIdAndSubSectionName(planId, subSectionName)
                .orElseThrow(() -> new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_NOT_FOUND));

        subSectionRepository.delete(subSection);

        return SubSectionResponse.Deleted.create(subSectionName, subSection.getId(), "deleted");
    }

    @Override
    public List<Boolean> checkSubSection(Long planId, @Valid SubSectionRequest request) {

        SubSectionName subSectionName = request.subSectionName();
        SubSection subSection = subSectionRepository.findByBusinessPlanIdAndSubSectionName(planId, subSectionName)
                .orElseThrow(() -> new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_NOT_FOUND));

        String content = PlainTextExtractUtils.extractPlainText(objectMapper, request);

        // RAG 기반 서브섹션별 체크리스트 수행
        List<Boolean> checks = checklistGrader.check(
                subSectionName,
                content);

        SubSectionSupportUtils.requireSize(List.of(), SubSectionName.getChecklistCount(subSectionName));

        subSection.updateChecks(checks);

        return checks;
    }

    /**
     * SubSectionName의 SectionName을 통해 BusinessPlan에서 해당 섹션 조회
     */
    private Object getParentSectionBySubSectionName(BusinessPlan businessPlan, SubSectionName subSectionName) {
        SectionName sectionName = subSectionName.getSection();

        return switch (sectionName) {
            case OVERVIEW -> businessPlan.getOverview();
            case PROBLEM_RECOGNITION -> businessPlan.getProblemRecognition();
            case FEASIBILITY -> businessPlan.getFeasibility();
            case GROWTH_STRATEGY -> businessPlan.getGrowthTactic();
            case TEAM_COMPETENCE -> businessPlan.getTeamCompetence();
        };
    }
}
