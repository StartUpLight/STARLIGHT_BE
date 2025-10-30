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
        BusinessPlan plan = businessPlanQuery.getOrThrow(planId);
        if (!plan.isOwnedBy(memberId)) {
            throw new BusinessPlanException(BusinessPlanErrorType.UNAUTHORIZED_ACCESS);
        }

        // Prevent orphan SubSections if DB doesn't enforce ON DELETE CASCADE
        deleteAllSubSectionsFor(plan);

        businessPlanQuery.delete(plan);
    }

    @Override
    public BusinessPlan updateBusinessPlanTitle(Long planId, Long memberId, String title) {
        BusinessPlan plan = businessPlanQuery.getOrThrow(planId);
        if (!plan.isOwnedBy(memberId)) {
            throw new BusinessPlanException(BusinessPlanErrorType.UNAUTHORIZED_ACCESS);
        }

        plan.updateTitle(title);

        return businessPlanQuery.save(plan);
    }

    @Override
    public SubSectionResponse.Created createOrUpdateSection(Long planId, JsonNode jsonNode,
            SubSectionName subSectionName) {
        BusinessPlan businessPlan = businessPlanQuery.getOrThrow(planId);

        String rawJsonStr = SubSectionSupportUtils.serializeJsonNodeSafely(objectMapper, jsonNode);
        String content = PlainTextExtractUtils.extractPlainText(objectMapper, jsonNode);

        Long parentSectionIdForQuery = getParentSectionId(businessPlan, subSectionName);
        SubSection subSection = businessPlanQuery
                .findSubSectionByParentSectionIdAndName(parentSectionIdForQuery, subSectionName)
                .orElse(null);

        String responseMessage;
        if (subSection == null) {
            subSection = SubSection.create(subSectionName, content, rawJsonStr);
            attachSubSectionToParent(subSection, businessPlan);
            responseMessage = "created";
        } else {
            subSection.updateContent(content, rawJsonStr);
            responseMessage = "updated";
        }

        SubSection savedSubSection = businessPlanQuery.saveSubSection(subSection);
        return SubSectionResponse.Created.create(subSectionName, savedSubSection.getId(), responseMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public SubSectionResponse.Retrieved getSubSection(Long planId, SubSectionName subSectionName) {
        BusinessPlan businessPlan = businessPlanQuery.getOrThrow(planId);
        Long parentSectionIdForQuery = getParentSectionId(businessPlan, subSectionName);
        SubSection subSection = businessPlanQuery
                .findSubSectionByParentSectionIdAndName(parentSectionIdForQuery, subSectionName)
                .orElseThrow(() -> new BusinessPlanException(BusinessPlanErrorType.SUBSECTION_NOT_FOUND));

        java.util.List<Boolean> checks = java.util.List.of(
                subSection.isCheckFirst(),
                subSection.isCheckSecond(),
                subSection.isCheckThird(),
                subSection.isCheckFourth(),
                subSection.isCheckFifth());

        return SubSectionResponse.Retrieved.create("retrieved", subSection.getRawJson().asTree(), checks);
    }

    @Override
    public SubSectionResponse.Deleted deleteSubSection(Long planId, SubSectionName subSectionName) {
        BusinessPlan businessPlan = businessPlanQuery.getOrThrow(planId);
        Long parentSectionIdForQuery = getParentSectionId(businessPlan, subSectionName);
        SubSection subSection = businessPlanQuery
                .findSubSectionByParentSectionIdAndName(parentSectionIdForQuery, subSectionName)
                .orElseThrow(() -> new BusinessPlanException(BusinessPlanErrorType.SUBSECTION_NOT_FOUND));

        businessPlanQuery.deleteSubSection(subSection);

        return SubSectionResponse.Deleted.create(subSectionName, subSection.getId(), "deleted");
    }

    @Override
    public List<Boolean> checkSubSection(Long planId, JsonNode jsonNode, SubSectionName subSectionName) {
        BusinessPlan businessPlan = businessPlanQuery.getOrThrow(planId);
        Long parentSectionIdForQuery = getParentSectionId(businessPlan, subSectionName);
        SubSection subSection = businessPlanQuery
                .findSubSectionByParentSectionIdAndName(parentSectionIdForQuery, subSectionName)
                .orElseThrow(() -> new BusinessPlanException(BusinessPlanErrorType.SUBSECTION_NOT_FOUND));

        String content = PlainTextExtractUtils.extractPlainText(objectMapper, jsonNode);

        List<Boolean> checks = checklistGrader.check(subSectionName, content);

        SubSectionSupportUtils.requireSize(checks, SubSection.getCHECKLIST_SIZE());

        subSection.updateChecks(checks);
        businessPlanQuery.saveSubSection(subSection);

        return checks;
    }

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

    private Long getParentSectionId(BusinessPlan businessPlan, SubSectionName subSectionName) {
        SectionName sectionName = subSectionName.getSectionName();
        return switch (sectionName) {
            case OVERVIEW -> businessPlan.getOverview().getId();
            case PROBLEM_RECOGNITION -> businessPlan.getProblemRecognition().getId();
            case FEASIBILITY -> businessPlan.getFeasibility().getId();
            case GROWTH_STRATEGY -> businessPlan.getGrowthTactic().getId();
            case TEAM_COMPETENCE -> businessPlan.getTeamCompetence().getId();
        };
    }

    private void deleteAllSubSectionsFor(BusinessPlan businessPlan) {
        Long overviewId = businessPlan.getOverview() != null ? businessPlan.getOverview().getId() : null;
        Long prId = businessPlan.getProblemRecognition() != null ? businessPlan.getProblemRecognition().getId() : null;
        Long feasId = businessPlan.getFeasibility() != null ? businessPlan.getFeasibility().getId() : null;
        Long growthId = businessPlan.getGrowthTactic() != null ? businessPlan.getGrowthTactic().getId() : null;
        Long teamId = businessPlan.getTeamCompetence() != null ? businessPlan.getTeamCompetence().getId() : null;

        if (overviewId != null)
            businessPlanQuery.deleteSubSectionsByParentSectionId(overviewId);
        if (prId != null)
            businessPlanQuery.deleteSubSectionsByParentSectionId(prId);
        if (feasId != null)
            businessPlanQuery.deleteSubSectionsByParentSectionId(feasId);
        if (growthId != null)
            businessPlanQuery.deleteSubSectionsByParentSectionId(growthId);
        if (teamId != null)
            businessPlanQuery.deleteSubSectionsByParentSectionId(teamId);
    }
}
