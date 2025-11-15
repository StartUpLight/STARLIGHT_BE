package starlight.application.businessplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.businessplan.provided.dto.BusinessPlanResponse;
import starlight.application.businessplan.provided.dto.SubSectionResponse;
import starlight.application.businessplan.provided.BusinessPlanService;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.application.businessplan.required.ChecklistGrader;
import starlight.application.businessplan.util.PlainTextExtractUtils;
import starlight.application.businessplan.util.SubSectionSupportUtils;
import starlight.domain.businessplan.entity.*;
import starlight.domain.businessplan.enumerate.PlanStatus;
import starlight.shared.enumerate.SectionType;
import starlight.domain.businessplan.enumerate.SubSectionType;
import starlight.domain.businessplan.exception.BusinessPlanErrorType;
import starlight.domain.businessplan.exception.BusinessPlanException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessPlanServiceImpl implements BusinessPlanService {

    private final BusinessPlanQuery businessPlanQuery;
    private final ChecklistGrader checklistGrader;
    private final ObjectMapper objectMapper;

    @Override
    public BusinessPlanResponse.Result createBusinessPlan(Long memberId) {
        BusinessPlan plan = BusinessPlan.create(memberId);

        return BusinessPlanResponse.Result.from(businessPlanQuery.save(plan), "Business plan created");
    }

    @Override
    public BusinessPlanResponse.Result createBusinessPlanWithPdf(String title, String pdfUrl, Long memberId) {
        BusinessPlan plan = BusinessPlan.createWithPdf(
                title,
                memberId,
                pdfUrl,
                PlanStatus.WRITTEN_COMPLETED
        );

        return BusinessPlanResponse.Result.from(businessPlanQuery.save(plan), "PDF Business plan created");
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessPlanResponse.Result getBusinessPlanInfo(Long planId, Long memberId) {
        BusinessPlan plan = getOwnedBusinessPlanOrThrow(planId, memberId);

        return BusinessPlanResponse.Result.from(plan, "Business plan retrieved");
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessPlanResponse.Detail getBusinessPlanDetail(Long planId, Long memberId) {
        BusinessPlan plan = getOwnedBusinessPlanOrThrow(planId, memberId);

        List<SubSectionResponse.Detail> subSectionDetailList = Arrays.stream(SubSectionType.values())
                .map(type -> getSectionByPlanAndType(plan, type.getSectionType()).getSubSectionByType(type))
                .filter(Objects::nonNull)
                .map(SubSectionResponse.Detail::from)
                .toList();

        return BusinessPlanResponse.Detail.from(plan, subSectionDetailList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessPlanResponse.Preview> getBusinessPlanList(Long memberId) {
        List<BusinessPlan> planList = businessPlanQuery.findAllByMemberIdOrderByModifiedAtDesc(memberId);

        return BusinessPlanResponse.Preview.fromAll(planList);
    }

    @Override
    public String updateBusinessPlanTitle(Long planId, String title, Long memberId) {
        BusinessPlan plan = getOwnedBusinessPlanOrThrow(planId, memberId);

        plan.updateTitle(title);

        businessPlanQuery.save(plan);

        return plan.getTitle();
    }

    @Override
    public BusinessPlanResponse.Result deleteBusinessPlan(Long planId, Long memberId) {
        BusinessPlan plan = getOwnedBusinessPlanOrThrow(planId, memberId);

        BusinessPlanResponse.Result result = BusinessPlanResponse.Result.from(plan, "Business plan deleted");
        businessPlanQuery.delete(plan);

        return result;
    }

    @Override
    public SubSectionResponse.Result createOrUpdateSubSection(
            Long planId,
            JsonNode jsonNode,
            List<Boolean> checks,
            SubSectionType subSectionType,
            Long memberId) {
        BusinessPlan plan = getOwnedBusinessPlanOrThrow(planId, memberId);

        SectionType sectionType = subSectionType.getSectionType();
        BaseSection section = getSectionByPlanAndType(plan, sectionType);
        SubSection subSection = section.getSubSectionByType(subSectionType);

        String rawJsonStr = getSerializedJsonNodesWithUpdatedChecks(jsonNode, checks);
        String content = PlainTextExtractUtils.extractPlainText(objectMapper, jsonNode);

        String message;

        if (subSection == null) {
            SubSection newSubSection = SubSection.create(subSectionType, content, rawJsonStr, checks);
            section.putSubSection(newSubSection);
            message = "Subsection created";
        } else {
            subSection.update(content, rawJsonStr, checks);
            message = "Subsection updated";
        }

        if (plan.areWritingCompleted()) {
            plan.updateStatus(PlanStatus.WRITTEN_COMPLETED);
            message = "Subsection writing completed";
        }

        BusinessPlan savedPlan = businessPlanQuery.save(plan);
        SubSection persistedSubSection = getSectionByPlanAndType(savedPlan, sectionType)
                .getSubSectionByType(subSectionType);

        return SubSectionResponse.Result.from(persistedSubSection, message);
    }

    @Override
    @Transactional(readOnly = true)
    public SubSectionResponse.Detail getSubSectionDetail(Long planId, SubSectionType subSectionType, Long memberId) {
        BusinessPlan plan = getOwnedBusinessPlanOrThrow(planId, memberId);

        SectionType sectionType = subSectionType.getSectionType();
        SubSection subSection = getSectionByPlanAndType(plan, sectionType).getSubSectionByType(subSectionType);
        if (subSection == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SUBSECTION_NOT_FOUND);
        }

        return SubSectionResponse.Detail.from(subSection);
    }

    @Override
    public List<Boolean> checkAndUpdateSubSection(
            Long planId,
            JsonNode jsonNode,
            SubSectionType subSectionType,
            Long memberId) {
        BusinessPlan plan = getOwnedBusinessPlanOrThrow(planId, memberId);

        SectionType sectionType = subSectionType.getSectionType();
        SubSection subSection = getSectionByPlanAndType(plan, sectionType).getSubSectionByType(subSectionType);
        if (subSection == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SUBSECTION_NOT_FOUND);
        }

        String newContent = PlainTextExtractUtils.extractPlainText(objectMapper, jsonNode);

        String previousContent = subSection.getContent();
        List<Boolean> previousChecks = subSection.getChecks();

        List<Boolean> checks = checklistGrader.check(subSectionType, newContent, previousContent, previousChecks);

        SubSectionSupportUtils.requireSize(checks, SubSection.getCHECKLIST_SIZE());
        String rawJsonStr = getSerializedJsonNodesWithUpdatedChecks(jsonNode, checks);

        subSection.update(newContent, rawJsonStr, checks);

        businessPlanQuery.save(plan);

        return checks;
    }

    @Override
    public SubSectionResponse.Result deleteSubSection(Long planId, SubSectionType subSectionType, Long memberId) {
        BusinessPlan plan = getOwnedBusinessPlanOrThrow(planId, memberId);

        SectionType sectionType = subSectionType.getSectionType();
        BaseSection section = getSectionByPlanAndType(plan, sectionType);
        SubSection target = section.getSubSectionByType(subSectionType);
        if (target == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SUBSECTION_NOT_FOUND);
        }
        SubSectionResponse.Result result = SubSectionResponse.Result.from(target, "Subsection deleted");
        section.removeSubSection(subSectionType);

        businessPlanQuery.save(plan);

        return result;
    }

    private String getSerializedJsonNodesWithUpdatedChecks(JsonNode jsonNode, List<Boolean> checks) {

        ObjectNode updatedJsonNode = (ObjectNode) objectMapper.valueToTree(jsonNode);

        ArrayNode checkListArray;
        if (updatedJsonNode.has("checks") && updatedJsonNode.get("checks").isArray()) {
            checkListArray = (ArrayNode) updatedJsonNode.get("checks");
            checkListArray.removeAll();

            for (Boolean check : checks) {
                checkListArray.add(check);
            }
        }

        return SubSectionSupportUtils.serializeJsonNodeSafely(objectMapper, updatedJsonNode);
    }

    private BusinessPlan getOwnedBusinessPlanOrThrow(Long planId, Long memberId) {
        BusinessPlan businessPlan = businessPlanQuery.getOrThrow(planId);
        if (!businessPlan.isOwnedBy(memberId)) {
            throw new BusinessPlanException(BusinessPlanErrorType.UNAUTHORIZED_ACCESS);
        }
        return businessPlan;
    }

    private BaseSection getSectionByPlanAndType(BusinessPlan plan, SectionType type) {
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
