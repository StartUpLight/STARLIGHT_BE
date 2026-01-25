package starlight.application.businessplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.businessplan.provided.dto.BusinessPlanResult;
import starlight.application.businessplan.provided.dto.SubSectionResult;
import starlight.application.businessplan.provided.BusinessPlanUseCase;
import starlight.application.businessplan.required.BusinessPlanCommandPort;
import starlight.application.businessplan.required.BusinessPlanQueryPort;
import starlight.application.businessplan.required.ChecklistGraderPort;
import starlight.application.businessplan.required.MemberLookUpPort;
import starlight.application.businessplan.util.PlainTextExtractUtils;
import starlight.application.businessplan.util.SubSectionSupportUtils;
import starlight.domain.businessplan.entity.*;
import starlight.domain.businessplan.enumerate.PlanStatus;
import starlight.domain.member.entity.Member;
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
public class BusinessPlanService implements BusinessPlanUseCase {

    private final BusinessPlanCommandPort businessPlanCommandPort;
    private final BusinessPlanQueryPort businessPlanQueryPort;
    private final MemberLookUpPort memberLookUpPort;
    private final ChecklistGraderPort checklistGrader;
    private final ObjectMapper objectMapper;

    @Override
    public BusinessPlanResult.Result createBusinessPlan(Long memberId) {
        Member member = memberLookUpPort.findByIdOrThrow(memberId);

        String planTitle = member.getName() == null ? "제목 없는 사업계획서" : member.getName() + "의 사업계획서";

        BusinessPlan plan = BusinessPlan.create(planTitle, memberId);

        return BusinessPlanResult.Result.from(businessPlanCommandPort.save(plan), "Business plan created");
    }

    @Override
    public BusinessPlanResult.Result createBusinessPlanWithPdf(String title, String pdfUrl, Long memberId) {
        BusinessPlan plan = BusinessPlan.createWithPdf(
                title,
                memberId,
                pdfUrl
        );

        return BusinessPlanResult.Result.from(businessPlanCommandPort.save(plan), "PDF Business plan created");
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessPlanResult.Result getBusinessPlanInfo(Long planId, Long memberId) {
        BusinessPlan plan = getOwnedBusinessPlanOrThrow(planId, memberId);

        return BusinessPlanResult.Result.from(plan, "Business plan retrieved");
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessPlanResult.Detail getBusinessPlanDetail(Long planId, Long memberId) {
        BusinessPlan plan = businessPlanQueryPort.findByIdWithAllSubSectionsOrThrow(planId);
        if (!plan.isOwnedBy(memberId)) {
            throw new BusinessPlanException(BusinessPlanErrorType.UNAUTHORIZED_ACCESS);
        }

        List<SubSectionResult.Detail> subSectionDetailList = Arrays.stream(SubSectionType.values())
                .map(type -> getSectionByPlanAndType(plan, type.getSectionType()).getSubSectionByType(type))
                .filter(Objects::nonNull)
                .map(SubSectionResult.Detail::from)
                .toList();

        return BusinessPlanResult.Detail.from(plan, subSectionDetailList);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessPlanResult.PreviewPage getBusinessPlanList(Long memberId, Pageable pageable) {
        Page<BusinessPlan> page = businessPlanQueryPort.findPreviewPage(memberId, pageable);
        List<BusinessPlanResult.Preview> content = page.getContent().stream()
                .map(BusinessPlanResult.Preview::from)
                .toList();

        return BusinessPlanResult.PreviewPage.from(content, page);
    }

    @Override
    public String updateBusinessPlanTitle(Long planId, String title, Long memberId) {
        BusinessPlan plan = getOwnedBusinessPlanOrThrow(planId, memberId);

        plan.updateTitle(title);

        businessPlanCommandPort.save(plan);

        return plan.getTitle();
    }

    @Override
    public BusinessPlanResult.Result deleteBusinessPlan(Long planId, Long memberId) {
        BusinessPlan plan = getOwnedBusinessPlanOrThrow(planId, memberId);

        BusinessPlanResult.Result result = BusinessPlanResult.Result.from(plan, "Business plan deleted");
        businessPlanCommandPort.delete(plan);

        return result;
    }

    @Override
    public SubSectionResult.Result upsertSubSection(
            Long planId,
            JsonNode jsonNode,
            List<Boolean> checks,
            SubSectionType subSectionType,
            Long memberId
    ) {
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

        BusinessPlan savedPlan = businessPlanCommandPort.save(plan);
        SubSection persistedSubSection = getSectionByPlanAndType(savedPlan, sectionType)
                .getSubSectionByType(subSectionType);

        return SubSectionResult.Result.from(persistedSubSection, message);
    }

    @Override
    @Transactional(readOnly = true)
    public SubSectionResult.Detail getSubSectionDetail(Long planId, SubSectionType subSectionType, Long memberId) {
        BusinessPlan plan = getOwnedBusinessPlanOrThrow(planId, memberId);

        SectionType sectionType = subSectionType.getSectionType();
        SubSection subSection = getSectionByPlanAndType(plan, sectionType).getSubSectionByType(subSectionType);
        if (subSection == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SUBSECTION_NOT_FOUND);
        }

        return SubSectionResult.Detail.from(subSection);
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

        String content = PlainTextExtractUtils.extractPlainText(objectMapper, jsonNode);

        List<Boolean> checks = checklistGrader.check(subSectionType, content);

        SubSectionSupportUtils.requireSize(checks, SubSection.getCHECKLIST_SIZE());
        String rawJsonStr = getSerializedJsonNodesWithUpdatedChecks(jsonNode, checks);

        subSection.update(content, rawJsonStr, checks);

        businessPlanCommandPort.save(plan);

        return checks;
    }

    @Override
    public SubSectionResult.Result deleteSubSection(Long planId, SubSectionType subSectionType, Long memberId) {
        BusinessPlan plan = getOwnedBusinessPlanOrThrow(planId, memberId);

        SectionType sectionType = subSectionType.getSectionType();
        BaseSection section = getSectionByPlanAndType(plan, sectionType);
        SubSection target = section.getSubSectionByType(subSectionType);
        if (target == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SUBSECTION_NOT_FOUND);
        }
        SubSectionResult.Result result = SubSectionResult.Result.from(target, "Subsection deleted");
        section.removeSubSection(subSectionType);

        businessPlanCommandPort.save(plan);

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
        BusinessPlan businessPlan = businessPlanQueryPort.findByIdOrThrow(planId);
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
