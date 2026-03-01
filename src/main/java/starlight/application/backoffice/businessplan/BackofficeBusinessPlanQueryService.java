package starlight.application.backoffice.businessplan;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import starlight.application.backoffice.businessplan.provided.BackofficeBusinessPlanQueryUseCase;
import starlight.application.backoffice.businessplan.provided.dto.result.BackofficeBusinessPlanDashboardResult;
import starlight.application.backoffice.businessplan.provided.dto.result.BackofficeBusinessPlanDetailResult;
import starlight.application.backoffice.businessplan.provided.dto.result.BackofficeBusinessPlanPageResult;
import starlight.application.backoffice.businessplan.provided.dto.result.BackofficeBusinessPlanRowResult;
import starlight.application.backoffice.businessplan.required.BackofficeBusinessPlanMemberLookupPort;
import starlight.application.backoffice.businessplan.required.BackofficeBusinessPlanQueryPort;
import starlight.application.backoffice.businessplan.required.BackofficeBusinessPlanScoreLookupPort;
import starlight.application.backoffice.businessplan.required.dto.BackofficeBusinessPlanMemberLookupResult;
import starlight.domain.businessplan.entity.BaseSection;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.SubSection;
import starlight.domain.businessplan.enumerate.PlanStatus;
import starlight.domain.businessplan.enumerate.SubSectionType;
import starlight.shared.enumerate.SectionType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BackofficeBusinessPlanQueryService implements BackofficeBusinessPlanQueryUseCase {

    private static final long NO_MATCH_MEMBER_ID = -1L;
    private static final int DEFAULT_TREND_DAYS = 7;

    private final BackofficeBusinessPlanQueryPort businessPlanQueryPort;
    private final BackofficeBusinessPlanMemberLookupPort memberLookupPort;
    private final BackofficeBusinessPlanScoreLookupPort scoreLookupPort;

    @Override
    public BackofficeBusinessPlanDetailResult findBusinessPlanDetail(Long planId) {
        BusinessPlan plan = businessPlanQueryPort.findByIdWithAllSubSectionsOrThrow(planId);
        Map<Long, Integer> scoreMap = scoreLookupPort.findScoresByBusinessPlanIds(List.of(planId));
        BackofficeBusinessPlanMemberLookupResult member = memberLookupPort.findBusinessPlanMemberById(plan.getMemberId());

        BackofficeBusinessPlanDetailResult.MemberResult memberResult = member == null
                ? null
                : BackofficeBusinessPlanDetailResult.MemberResult.of(
                member.memberId(),
                member.name(),
                member.email(),
                member.provider(),
                member.joinedAt(),
                member.lastActiveAt()
        );

        Integer score = scoreMap.get(planId);
        Double normalizedScore = score != null ? Double.valueOf(score) : null;

        return BackofficeBusinessPlanDetailResult.of(
                plan.getId(),
                plan.getTitle(),
                plan.getPlanStatus(),
                normalizedScore,
                plan.getCreatedAt(),
                resolveUpdatedAt(plan),
                memberResult,
                buildSubSectionDetailList(plan)
        );
    }

    @Override
    public BackofficeBusinessPlanPageResult findBusinessPlans(PlanStatus status, String keyword, Pageable pageable) {
        String normalizedKeyword = normalizeKeyword(keyword);
        List<Long> memberIds = resolveMemberIds(normalizedKeyword);

        Page<BusinessPlan> page = businessPlanQueryPort.findBusinessPlanPage(
                status,
                normalizedKeyword,
                memberIds,
                pageable
        );

        List<Long> planIds = page.getContent().stream()
                .map(BusinessPlan::getId)
                .toList();

        List<Long> memberIdList = page.getContent().stream()
                .map(BusinessPlan::getMemberId)
                .distinct()
                .toList();

        Map<Long, BackofficeBusinessPlanMemberLookupResult> memberMap = memberLookupPort.findBusinessPlanMembersByIds(memberIdList);
        Map<Long, Integer> scoreMap = scoreLookupPort.findScoresByBusinessPlanIds(planIds);

        List<BackofficeBusinessPlanRowResult> content = page.getContent().stream()
                .map(plan -> {
                    BackofficeBusinessPlanMemberLookupResult member = memberMap.get(plan.getMemberId());

                    return BackofficeBusinessPlanRowResult.of(
                            plan.getId(),
                            plan.getTitle(),
                            plan.getPlanStatus(),
                            plan.getMemberId(),
                            member != null ? member.name() : null,
                            member != null ? member.email() : null,
                            member != null ? member.provider() : null,
                            resolveUpdatedAt(plan),
                            scoreMap.get(plan.getId())
                    );
                })
                .toList();

        return BackofficeBusinessPlanPageResult.of(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }

    @Override
    public BackofficeBusinessPlanDashboardResult getDashboard(
            PlanStatus status,
            String keyword,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        String normalizedKeyword = normalizeKeyword(keyword);
        List<Long> memberIds = resolveMemberIds(normalizedKeyword);

        LocalDate today = LocalDate.now();
        LocalDate defaultFromDate = today.minusDays(DEFAULT_TREND_DAYS - 1L);

        LocalDate trendFromDate = dateFrom != null ? dateFrom : defaultFromDate;
        LocalDate trendToDate = dateTo != null ? dateTo : today;

        if (trendFromDate.isAfter(trendToDate)) {
            LocalDate temp = trendFromDate;
            trendFromDate = trendToDate;
            trendToDate = temp;
        }

        LocalDateTime from = trendFromDate.atStartOfDay();
        LocalDateTime to = trendToDate.plusDays(1).atStartOfDay();

        List<BusinessPlan> plans = businessPlanQueryPort.findBusinessPlansForDashboard(
                status,
                normalizedKeyword,
                memberIds,
                from,
                to
        );

        List<Long> planIds = plans.stream().map(BusinessPlan::getId).toList();
        Map<Long, Integer> scoreMap = scoreLookupPort.findScoresByBusinessPlanIds(planIds);

        long totalCount = plans.size();
        long scoredCount = plans.stream().filter(plan -> scoreMap.get(plan.getId()) != null).count();
        long unscoredCount = totalCount - scoredCount;

        Double averageScore = null;
        if (scoredCount > 0) {
            int scoreSum = plans.stream()
                    .map(BusinessPlan::getId)
                    .map(scoreMap::get)
                    .filter(score -> score != null)
                    .mapToInt(Integer::intValue)
                    .sum();
            averageScore = (double) scoreSum / scoredCount;
        }

        BackofficeBusinessPlanDashboardResult.StatsResult stats =
                BackofficeBusinessPlanDashboardResult.StatsResult.of(
                        totalCount,
                        scoredCount,
                        unscoredCount,
                        averageScore
                );

        Map<LocalDate, Long> trendCountMap = new HashMap<>();
        for (BusinessPlan plan : plans) {
            LocalDate date = resolveUpdatedAt(plan).toLocalDate();
            trendCountMap.merge(date, 1L, Long::sum);
        }

        List<BackofficeBusinessPlanDashboardResult.TrendPointResult> trend = trendFromDate.datesUntil(trendToDate.plusDays(1))
                .map(date -> BackofficeBusinessPlanDashboardResult.TrendPointResult.of(
                        date,
                        trendCountMap.getOrDefault(date, 0L)
                ))
                .toList();

        Map<PlanStatus, Long> statusCountMap = new EnumMap<>(PlanStatus.class);
        for (BusinessPlan plan : plans) {
            statusCountMap.merge(plan.getPlanStatus(), 1L, Long::sum);
        }

        List<BackofficeBusinessPlanDashboardResult.StatusDistributionItemResult> statusDistribution =
                Arrays.stream(PlanStatus.values())
                        .map(statusKey -> {
                            long count = statusCountMap.getOrDefault(statusKey, 0L);
                            return BackofficeBusinessPlanDashboardResult.StatusDistributionItemResult.of(
                                    statusKey,
                                    toStatusLabel(statusKey),
                                    count,
                                    ratio(count, totalCount)
                            );
                        })
                        .toList();

        List<BackofficeBusinessPlanDashboardResult.ScoreDistributionItemResult> scoreDistribution =
                buildScoreDistribution(scoreMap, planIds, totalCount);

        return BackofficeBusinessPlanDashboardResult.of(
                stats,
                trend,
                statusDistribution,
                scoreDistribution
        );
    }

    private List<BackofficeBusinessPlanDashboardResult.ScoreDistributionItemResult> buildScoreDistribution(
            Map<Long, Integer> scoreMap,
            List<Long> planIds,
            long totalCount
    ) {
        if (scoreMap.isEmpty() && (planIds == null || planIds.isEmpty())) {
            return List.of();
        }

        long unscored = 0L;
        long score0To59 = 0L;
        long score60To79 = 0L;
        long score80To100 = 0L;

        for (Long planId : planIds) {
            Integer score = scoreMap.get(planId);
            if (score == null) {
                unscored++;
                continue;
            }

            if (score <= 59) {
                score0To59++;
            } else if (score <= 79) {
                score60To79++;
            } else {
                score80To100++;
            }
        }

        return List.of(
                BackofficeBusinessPlanDashboardResult.ScoreDistributionItemResult.of(
                        "UNSCORED",
                        "미채점",
                        unscored,
                        ratio(unscored, totalCount)
                ),
                BackofficeBusinessPlanDashboardResult.ScoreDistributionItemResult.of(
                        "SCORE_0_59",
                        "0-59점",
                        score0To59,
                        ratio(score0To59, totalCount)
                ),
                BackofficeBusinessPlanDashboardResult.ScoreDistributionItemResult.of(
                        "SCORE_60_79",
                        "60-79점",
                        score60To79,
                        ratio(score60To79, totalCount)
                ),
                BackofficeBusinessPlanDashboardResult.ScoreDistributionItemResult.of(
                        "SCORE_80_100",
                        "80-100점",
                        score80To100,
                        ratio(score80To100, totalCount)
                )
        );
    }

    private List<BackofficeBusinessPlanDetailResult.SubSectionDetailResult> buildSubSectionDetailList(BusinessPlan plan) {
        Map<SectionType, Integer> sectionItemCounter = new EnumMap<>(SectionType.class);
        int displayOrder = 1;
        List<BackofficeBusinessPlanDetailResult.SubSectionDetailResult> items = new ArrayList<>();

        for (SubSectionType subSectionType : SubSectionType.values()) {
            SubSection subSection = getSectionByPlanAndType(plan, subSectionType.getSectionType())
                    .getSubSectionByType(subSectionType);
            if (subSection == null) {
                continue;
            }

            int sectionIndex = sectionItemCounter.merge(subSectionType.getSectionType(), 1, Integer::sum);
            JsonNode raw = subSection.getRawJson().asTree();
            String displayTitle = firstText(raw, "title", "name");

            items.add(BackofficeBusinessPlanDetailResult.SubSectionDetailResult.of(
                    subSectionType,
                    subSection.getId(),
                    displayOrder,
                    sectionNumber(subSectionType.getSectionType()) + "-" + sectionIndex,
                    subSectionType.getSectionType().getDescription(),
                    StringUtils.hasText(displayTitle) ? displayTitle : subSectionType.getDescription(),
                    buildChecklist(raw),
                    raw
            ));

            displayOrder++;
        }

        return items;
    }

    private List<BackofficeBusinessPlanDetailResult.ChecklistResult> buildChecklist(JsonNode raw) {
        List<JsonNode> checklistNodes = extractArray(raw, "checklist");
        if (!checklistNodes.isEmpty()) {
            List<BackofficeBusinessPlanDetailResult.ChecklistResult> checklist = new ArrayList<>();
            int index = 1;
            for (JsonNode node : checklistNodes) {
                String title = firstText(node, "title", "label", "name");
                String content = firstText(node, "content", "value", "text");
                boolean checked = node.path("checked").asBoolean(false);
                checklist.add(BackofficeBusinessPlanDetailResult.ChecklistResult.of(
                        StringUtils.hasText(title) ? title : "체크리스트 " + index,
                        content,
                        checked
                ));
                index++;
            }
            return checklist;
        }

        List<JsonNode> checks = extractArray(raw, "checks");
        List<BackofficeBusinessPlanDetailResult.ChecklistResult> checklist = new ArrayList<>();
        for (int i = 0; i < checks.size(); i++) {
            checklist.add(BackofficeBusinessPlanDetailResult.ChecklistResult.of(
                    "체크리스트 " + (i + 1),
                    null,
                    checks.get(i).asBoolean(false)
            ));
        }
        return checklist;
    }

    private BaseSection getSectionByPlanAndType(BusinessPlan plan, SectionType sectionType) {
        return switch (sectionType) {
            case OVERVIEW -> plan.getOverview();
            case PROBLEM_RECOGNITION -> plan.getProblemRecognition();
            case FEASIBILITY -> plan.getFeasibility();
            case GROWTH_STRATEGY -> plan.getGrowthTactic();
            case TEAM_COMPETENCE -> plan.getTeamCompetence();
        };
    }

    private static String firstText(JsonNode node, String... fieldNames) {
        if (node == null || node.isMissingNode()) {
            return null;
        }

        for (String fieldName : fieldNames) {
            JsonNode child = node.path(fieldName);
            if (child.isTextual() && StringUtils.hasText(child.asText())) {
                return child.asText();
            }
        }

        return null;
    }

    private static List<JsonNode> extractArray(JsonNode root, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode node = root.path(fieldName);
            if (node.isArray()) {
                List<JsonNode> list = new ArrayList<>();
                node.forEach(list::add);
                return list;
            }
        }

        return List.of();
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        return keyword.trim();
    }

    private List<Long> resolveMemberIds(String keyword) {
        if (keyword == null) {
            return List.of(NO_MATCH_MEMBER_ID);
        }

        List<Long> memberIds = memberLookupPort.findMemberIdsByKeyword(keyword);
        if (memberIds == null || memberIds.isEmpty()) {
            return List.of(NO_MATCH_MEMBER_ID);
        }

        return memberIds;
    }

    private LocalDateTime resolveUpdatedAt(BusinessPlan plan) {
        return plan.getModifiedAt() != null ? plan.getModifiedAt() : plan.getCreatedAt();
    }

    private String toStatusLabel(PlanStatus status) {
        return switch (status) {
            case STARTED -> "작성중";
            case WRITTEN_COMPLETED -> "작성완료";
            case AI_REVIEWED -> "AI 리뷰완료";
            case EXPERT_MATCHED -> "전문가 매칭완료";
            case FINALIZED -> "최종완료";
        };
    }

    private double ratio(long count, long total) {
        if (total <= 0L) {
            return 0.0;
        }

        return (double) count / total;
    }

    private static String sectionNumber(SectionType sectionType) {
        return switch (sectionType) {
            case OVERVIEW -> "0";
            case PROBLEM_RECOGNITION -> "1";
            case FEASIBILITY -> "2";
            case GROWTH_STRATEGY -> "3";
            case TEAM_COMPETENCE -> "4";
        };
    }
}
