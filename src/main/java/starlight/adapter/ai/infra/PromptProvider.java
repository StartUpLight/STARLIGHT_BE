package starlight.adapter.ai.infra;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PromptProvider {

  @Value("${prompt.report.grading.system}")
  private String reportGradingSystemPrompt;

  @Value("${prompt.report.grading.user}")
  private String reportGradingUserPromptTemplate;

  @Value("${prompt.checklist.grading.system}")
  private String checklistGradingSystemPrompt;

  @Value("${prompt.checklist.grading.user.template}")
  private String checklistGradingUserPromptTemplate;

  /**
   * 리포트 채점용 Prompt 객체 생성
   */
  public Prompt createReportGradingPrompt(String businessPlanContent) {
    Message systemMessage = new SystemMessage(getReportGradingSystemPrompt());
    Message userMessage = new UserMessage(buildReportGradingUserPrompt(businessPlanContent));
    return new Prompt(List.of(systemMessage, userMessage));
  }

  /**
   * 체크리스트 채점용 Prompt 객체 생성
   */
  public Prompt createChecklistGradingPrompt(
      SubSectionType subSectionType,
      String content,
      List<String> criteria,
      List<String> detailedCriteria
  ) {
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
   * 리포트 채점용 사용자 프롬프트 생성
   */
  private String buildReportGradingUserPrompt(String businessPlanContent) {
    PromptTemplate promptTemplate = new PromptTemplate(reportGradingUserPromptTemplate);
    Map<String, Object> variables = Map.of("businessPlanContent", businessPlanContent);
    return promptTemplate.render(variables);
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
