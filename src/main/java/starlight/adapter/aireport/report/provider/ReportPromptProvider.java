package starlight.adapter.aireport.reportgrader.provider;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import starlight.domain.businessplan.enumerate.SubSectionType;
import starlight.shared.enumerate.SectionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReportPromptProvider {

  // 사업계획서 전체 채점 프롬프트
  @Value("${prompt.report.grading.system}")
  private String reportGradingSystemPrompt;

  // 사업계획서 섹션별 채점 공통 프롬프트
  @Value("${prompt.report.section.default.system}")
  private String sectionDefaultSystemPrompt;

  // 사업계획서 섹션별 채점 프롬프트
  @Value("${prompt.report.section.problem_recognition.system}")
  private String problemRecognitionSystemPrompt;

  @Value("${prompt.report.section.feasibility.system}")
  private String feasibilitySystemPrompt;

  @Value("${prompt.report.section.growth_strategy.system}")
  private String growthStrategySystemPrompt;

  @Value("${prompt.report.section.team_competence.system}")
  private String teamCompetenceSystemPrompt;

  @Value("${prompt.checklist.grading.system}")
  private String checklistGradingSystemPrompt;

  @Value("${prompt.checklist.grading.user.template}")
  private String checklistGradingUserPromptTemplate;

  /**
   * 리포트 채점용 Prompt 객체 생성
   * @deprecated 전체 리포트를 한 번에 채점하는 방식은 더 이상 권장되지 않습니다. 
   *             대신 {@link #createSectionGradingPrompt(SectionType, String)}를 사용하여 섹션별로 채점하는 방식을 사용하세요.
   */
  @Deprecated
  public Prompt createReportGradingPrompt(String businessPlanContent) {
    Message systemMessage = new SystemMessage(getReportGradingSystemPrompt());
    Message userMessage = new UserMessage(businessPlanContent); // 사업계획서 내용만 직접 전달
    return new Prompt(List.of(systemMessage, userMessage));
  }

  /**
   * 섹션별 채점용 Prompt 객체 생성
   */
  public Prompt createSectionGradingPrompt(SectionType sectionType, String sectionContent) {
    String systemPrompt = getSectionGradingSystemPrompt(sectionType);

    Message systemMessage = new SystemMessage(systemPrompt);
    Message userMessage = new UserMessage(sectionContent); // 섹션 내용만 직접 전달
    return new Prompt(List.of(systemMessage, userMessage));
  }

  /**
   * 체크리스트 채점용 Prompt 객체 생성
   */
  public Prompt createChecklistGradingPrompt(
      SubSectionType subSectionType,
      String content,
      List<String> criteria,
      List<String> detailedCriteria) {
    String userPrompt = buildChecklistGradingUserPrompt(subSectionType, content, criteria, detailedCriteria);
    Message systemMessage = new SystemMessage(checklistGradingSystemPrompt);
    Message userMessage = new UserMessage(userPrompt);
    return new Prompt(List.of(systemMessage, userMessage));
  }

  /**
   * 리포트 채점용 시스템 프롬프트
   */
  private String getReportGradingSystemPrompt() {
    return reportGradingSystemPrompt;
  }

  /**
   * 섹션별 채점용 시스템 프롬프트
   * 공통 프롬프트와 섹션별 프롬프트를 합쳐서 반환
   */
  private String getSectionGradingSystemPrompt(SectionType sectionType) {
    String sectionSpecificPrompt = switch (sectionType) {
      case PROBLEM_RECOGNITION -> problemRecognitionSystemPrompt;
      case FEASIBILITY -> feasibilitySystemPrompt;
      case GROWTH_STRATEGY -> growthStrategySystemPrompt;
      case TEAM_COMPETENCE -> teamCompetenceSystemPrompt;
      default -> ""; // 기본값
    };
    
    // 공통 프롬프트와 섹션별 프롬프트를 합침
    return sectionDefaultSystemPrompt + "\n\n" + sectionSpecificPrompt;
  }

  /**
   * 체크리스트 채점용 사용자 프롬프트 생성
   */
  private String buildChecklistGradingUserPrompt(
      SubSectionType subSectionType,
      String content,
      List<String> criteria,
      List<String> detailedCriteria) {
    // 체크리스트 상세 기준 포맷팅
    StringBuilder criteriaBuilder = new StringBuilder();
    for (int i = 0; i < criteria.size() && i < detailedCriteria.size(); i++) {
      criteriaBuilder.append(i + 1).append(") ").append(criteria.get(i)).append("\n");
      criteriaBuilder.append(detailedCriteria.get(i)).append("\n\n");
    }
    String formattedCriteria = criteriaBuilder.toString().trim();

    Map<String, Object> variables = new HashMap<>();
    variables.put("subsectionType", subSectionType.getDescription());
    variables.put("checklistCriteria", formattedCriteria);
    variables.put("input", content);
    variables.put("requestLength", criteria.size());

    PromptTemplate promptTemplate = new PromptTemplate(checklistGradingUserPromptTemplate);
    return promptTemplate.render(variables);
  }
}
