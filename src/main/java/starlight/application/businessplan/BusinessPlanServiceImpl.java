package starlight.application.businessplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.businessplan.dto.SubSectionResponse;
import starlight.application.businessplan.provided.BusinessPlanService;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.application.businessplan.required.ChecklistGrader;
import starlight.application.businessplan.util.PlainTextExtractUtils;
import starlight.application.businessplan.util.SubSectionSupportUtils;
import starlight.domain.businessplan.entity.*;
import starlight.shared.domain.enumerate.SectionType;
import starlight.domain.businessplan.enumerate.SubSectionType;
import starlight.domain.businessplan.exception.BusinessPlanErrorType;
import starlight.domain.businessplan.exception.BusinessPlanException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessPlanServiceImpl implements BusinessPlanService {

    private final BusinessPlanQuery businessPlanQuery;
    private final ChecklistGrader checklistGrader;
    private final ObjectMapper objectMapper;

    @Override
    public BusinessPlan createBusinessPlan(Long memberId) {
        BusinessPlan plan = BusinessPlan.create(memberId);

        return businessPlanQuery.save(plan);
    }

    @Override
    public void deleteBusinessPlan(Long planId, Long memberId) {
        BusinessPlan plan = getOwnedBusinessPlanOrThrow(planId, memberId);

        businessPlanQuery.delete(plan);
    }

    @Override
    public BusinessPlan updateBusinessPlanTitle(Long planId, Long memberId, String title) {
        BusinessPlan plan = getOwnedBusinessPlanOrThrow(planId, memberId);

        plan.updateTitle(title);

        return businessPlanQuery.save(plan);
    }

    @Override
    public SubSectionResponse.Created createOrUpdateSubSection(Long planId, JsonNode jsonNode, SubSectionType subSectionType, Long memberId) {
        BusinessPlan plan = getOwnedBusinessPlanOrThrow(planId, memberId);

        SectionType sectionType = subSectionType.getSectionType();
        BaseSection section = getSectionByPlanAndType(plan, sectionType);
        SubSection subSection = section.getSubSectionByType(subSectionType);

        String rawJsonStr = SubSectionSupportUtils.serializeJsonNodeSafely(objectMapper, jsonNode);
        String content = PlainTextExtractUtils.extractPlainText(objectMapper, jsonNode);

        SubSection targetSubSection;
        String message;

        if (subSection == null) {
            SubSection newSubSection = SubSection.create(subSectionType, content, rawJsonStr);
            section.putSubSection(newSubSection);
            targetSubSection = newSubSection;
            message = "created";
        } else {
            subSection.updateContent(content, rawJsonStr);
            targetSubSection = subSection;
            message = "updated";
        }

        businessPlanQuery.save(plan);

        return SubSectionResponse.Created.create(subSectionType, targetSubSection.getId(), message);
    }

    @Override
    @Transactional(readOnly = true)
    public SubSectionResponse.Retrieved getSubSection(Long planId, SubSectionType subSectionType, Long memberId) {
        BusinessPlan plan = getOwnedBusinessPlanOrThrow(planId, memberId);

        SectionType sectionType = subSectionType.getSectionType();
        SubSection subSection = getSectionByPlanAndType(plan, sectionType).getSubSectionByType(subSectionType);
        if (subSection == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SUBSECTION_NOT_FOUND);
        }

        List<Boolean> checks = List.of(
                subSection.isCheckFirst(),
                subSection.isCheckSecond(),
                subSection.isCheckThird(),
                subSection.isCheckFourth(),
                subSection.isCheckFifth()
        );
        return SubSectionResponse.Retrieved.create(
                "retrieved",
                subSection.getRawJson().asTree(),
                checks
        );
    }

    @Override
    public SubSectionResponse.Deleted deleteSubSection(Long planId, SubSectionType subSectionType, Long memberId) {
        BusinessPlan plan = getOwnedBusinessPlanOrThrow(planId, memberId);

        SectionType sectionType = subSectionType.getSectionType();
        BaseSection section = getSectionByPlanAndType(plan, sectionType);
        SubSection target = section.getSubSectionByType(subSectionType);
        if (target == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SUBSECTION_NOT_FOUND);
        }
        section.removeSubSection(subSectionType);

        businessPlanQuery.save(plan);

        return SubSectionResponse.Deleted.create(subSectionType, null, "deleted");
    }

    @Override
    public List<Boolean> checkAndUpdateSubSection(Long planId, JsonNode jsonNode, SubSectionType subSectionType, Long memberId) {
        BusinessPlan plan = getOwnedBusinessPlanOrThrow(planId, memberId);

        String rawJsonStr = SubSectionSupportUtils.serializeJsonNodeSafely(objectMapper, jsonNode);
        String content = PlainTextExtractUtils.extractPlainText(objectMapper, jsonNode);

        SectionType sectionType = subSectionType.getSectionType();
        SubSection subSection = getSectionByPlanAndType(plan, sectionType).getSubSectionByType(subSectionType);
        if (subSection == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SUBSECTION_NOT_FOUND);
        }

        List<Boolean> checks = checklistGrader.check(subSectionType, content);
        SubSectionSupportUtils.requireSize(checks, SubSection.getCHECKLIST_SIZE());
        subSection.updateContent(content, rawJsonStr);
        subSection.updateChecks(checks);

        businessPlanQuery.save(plan);

        return checks;
    }

    private BusinessPlan getOwnedBusinessPlanOrThrow(Long planId, Long memberId) {
        BusinessPlan businessPlan = businessPlanQuery.getOrThrow(planId);
        if (!businessPlan.isOwnedBy(memberId)) {
            throw new BusinessPlanException(BusinessPlanErrorType.UNAUTHORIZED_ACCESS);
        }
        return businessPlan;
    }

    private BaseSection getSectionByPlanAndType(BusinessPlan plan, SectionType type){
        return switch (type) {
            case OVERVIEW -> plan.getOverview();
            case PROBLEM_RECOGNITION -> plan.getProblemRecognition();
            case FEASIBILITY -> plan.getFeasibility();
            case GROWTH_STRATEGY -> plan.getGrowthTactic();
            case TEAM_COMPETENCE -> plan.getTeamCompetence();
            default -> throw new IllegalArgumentException("Unsupported section: " + type);
        };
    }
}
