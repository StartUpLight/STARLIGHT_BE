package starlight.adapter.aireport.report.provider;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import starlight.shared.enumerate.SectionType;

import java.util.List;

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

  /**
   * 리포트 채점용 Prompt 객체 생성
   */
  public Prompt createReportGradingPrompt(String businessPlanContent) {
    Message systemMessage = new SystemMessage(reportGradingSystemPrompt);
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
}
