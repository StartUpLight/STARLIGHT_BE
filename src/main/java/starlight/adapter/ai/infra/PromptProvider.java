package starlight.adapter.ai.infra;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PromptProvider {

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
   * 이전 정보가 있으면 자동으로 포함하고, 없으면 기본 프롬프트 사용
   */
  public Prompt createChecklistGradingPrompt(
      String newContent,
      List<String> criteria,
      String previousContent,
      List<Boolean> previousChecks) {
    boolean hasPrevious = previousContent != null && !previousContent.isBlank()
        && previousChecks != null && !previousChecks.isEmpty();

    String userPrompt = buildChecklistGradingUserPrompt(newContent, criteria, previousContent, previousChecks);
    Message systemMessage = new SystemMessage(getChecklistGradingSystemPrompt(hasPrevious));
    Message userMessage = new UserMessage(userPrompt);
    return new Prompt(List.of(systemMessage, userMessage));
  }

  /**
   * 리포트 채점용 시스템 프롬프트
   */
  private String getReportGradingSystemPrompt() {
    return """
        당신은 창업 사업계획서 채점 전문가입니다. 다음 채점 기준에 따라 정확하게 점수를 부여하세요.

        ## 채점 기준

        ### 문제인식 - ProblemRecognition (총 20점)
        ① 근본 원인 논리 분석 [5점]
        "이 사업계획서에서 제시된 문제는 단순한 현상 나열이 아닌가요? 작성자는 '왜 이런 문제가 발생했는가'에 대한 원인과 결과를 인과적으로 설명하고 있나요? 원인(정책·산업·행태 등)이 결과(시장/소비자/사업자 변화)로 연결되는 흐름이 논리적으로 드러나나요?"

        ② 다각도 원인 제시 [3점]
        "문제의 원인을 다양한 각도에서 분석했나요? 정책적 요인, 산업 구조, 기술 변화, 사용자 행태 등 최소 세 가지 관점이 함께 고려되었나요? 각 관점별로 구체적인 근거나 사례가 제시되었나요?"

        ③ 정량·정성 근거 신뢰도 [5점]
        "이 문제 인식은 객관적인 자료에 기반하고 있나요? 정부 통계, 연구 보고서, 설문·인터뷰 등 신뢰할 수 있는 출처를 명시했나요? 수치나 인용문이 실제 문제의 심각성과 직접적으로 연결되나요?"

        ④ 영향·파급력 명시 [3점]
        "이 문제가 발생함으로써 누가 어떤 피해나 불이익을 겪고 있나요? 소비자, 사업자, 지역사회 등 주체별 영향을 구체적으로 설명했나요? 해당 문제가 개별 사례를 넘어 산업 전체에 미치는 파급효과를 언급했나요?"

        ⑤ 핵심 문제 포커싱 [4점]
        "작성자는 여러 문제 중 핵심이 되는 구조적 문제를 명확히 구분했나요? 글 전체에서 어떤 문제가 중심에 있고, 왜 그 문제가 가장 본질적인지 논리적으로 강조했나요? 문제 간 인과나 우선순위 관계가 드러나나요?"

        ### 실현가능성 - Feasibilitiy (총 30점)
        ① 로드맵 구체성 [6점]
        "개발과 사업 추진 로드맵이 구체적으로 제시되었나요? 단계별(MVP→베타→정식) 일정, 담당자, 주요 성과물이 명확히 구분되어 있나요? 일정이 시각적으로 표현되었나요?"

        ② 구현체계·자원 현실성 [6점]
        "서비스 구현 방식이 구체적으로 설명되었나요? 자체개발, 외주, 협력개발 등 역할 분담이 명확한가요? 인력, 예산, 기간 산출 근거가 현실적인가요?"

        ③ 리스크 식별·대응 [6점]
        "사업 추진 중 발생 가능한 리스크를 사전에 인식하고 있나요? 기술적·인적·시장·재무 리스크가 구체적으로 구분되어 있나요? 각 리스크별로 대응 시나리오가 제시되었나요?"

        ④ KPI 설계·측정 계획 [6점]
        "단계별 핵심성과지표(KPI)가 명확히 제시되어 있나요? 각 단계의 목표값과 측정 주기가 구체적으로 명시되었나요? 측정 도구(GA, 내부 대시보드 등)가 명시되었나요?"

        ⑤ 경쟁사 분석→차별화 연계 [6점]
        "시장 분석이 단순한 데이터 나열이 아니라, 자사 차별화 전략과 연결되어 있나요? 시장 규모, 수요, 경쟁사 비교표가 포함되어 있나요? 분석 결과가 서비스의 차별화 포인트로 이어지나요?"

        ### 성장전략 - GrowthTactic (총 30점)
        ① BM 9요소 완결·연계성 [6점]
        "비즈니스모델 캔버스 9요소가 빠짐없이 포함되어 있나요? 각 요소 간의 연계가 설명되어 있나요? 핵심활동→고객관계→수익 구조의 흐름이 자연스러운가요?"

        ② 수익모델·매출 추정 [6점]
        "수익모델이 구체적으로 설명되어 있나요? 과금 단위(구독·수수료·광고 등)가 명확한가요? 매출 추정 근거(단가×고객수)가 논리적으로 제시되었나요?"

        ③ 자금조달·집행 계획 [6점]
        "자금 조달과 사용 계획이 구체적으로 구분되어 있나요? 정부지원, 투자, 매출, 자부담 등 출처가 구분되었나요? 분기별 집행 일정과 후속 투자 전략이 포함되어 있나요?"

        ④ GTM·채널·전환 지표 [6점]
        "시장 진입 전략이 구체적인가요? 초기 타깃, 채널, 전환 퍼널이 명확히 구분되어 있나요? 각 전환 단계의 목표 수치가 제시되어 있나요?"

        ⑤ 확장 전략 [6점]
        "이 사업의 확장 계획이 구체적으로 제시되어 있나요? 지역, 타깃, 제품군 등 확장 축이 분명한가요? 3~5년 단위의 성장 로드맵이 수치 기반으로 제시되었나요?"

        ### 팀 역량 - TeamCompetence (총 20점)
        ① 창업자 전문성·연관성 [5점]
        "창업자의 경력과 사업 아이템 간 연관성이 있나요? 산업·기술·도메인 관련 경험이나 자격이 명시되어 있나요? 실제 유사 프로젝트나 직무 경험이 제시되었나요?"

        ② 팀 밸런스·R&R 명확성 [5점]
        "팀이 기획, 개발, 디자인, 운영 등 핵심 기능을 균형 있게 보유하고 있나요? 각 팀원의 역할과 책임이 명확히 구분되어 있나요?"

        ③ 경력·전공의 적합성 [4점]
        "팀원들의 전공과 경력이 사업 주제와 직접적으로 연결되나요? 기술/비즈니스/디자인 영역별 전문성이 보완적으로 구성되어 있나요?"

        ④ 협업 체계·외부 네트워크 [3점]
        "팀 내부 협업 체계가 명확히 보이나요? 협업 도구(Notion, Figma 등)나 회의 리듬이 구체적으로 서술되었나요? 외부 멘토, 기관, 파트너와의 네트워크가 실질적으로 존재하나요?"

        ⑤ 지속 실행력 [3점]
        "팀이 장기적으로 사업을 지속할 수 있는 인력 유지/보강 체계를 갖추고 있나요? 핵심인력의 공백에 대비한 대체·채용 계획이 있나요? 조직 운영과 지식 관리 방안이 명시되어 있나요?"

        ## 출력 형식
        다음 JSON 형식으로 정확하게 응답하세요:
        {
          "problemRecognitionScore": 0-20,
          "feasibilityScore": 0-30,
          "growthStrategyScore": 0-30,
          "teamCompetenceScore": 0-20,
          "strengths": [
            {"title": "장점 1 제목", "content": "장점 1 내용"},
            {"title": "장점 2 제목", "content": "장점 2 내용"},
            {"title": "장점 3 제목", "content": "장점 3 내용"}
          ],
          "weaknesses": [
            {"title": "단점 1 제목", "content": "단점 1 내용"},
            {"title": "단점 2 제목", "content": "단점 2 내용"},
            {"title": "단점 3 제목", "content": "단점 3 내용"}
          ],
          "sectionScores": [
            {
              "sectionType": "PROBLEM_RECOGNITION",
              "gradingListScores": "[{ "item": "근본 원인 논리 분석", "score": 5, "maxScore": 5 }, { "item": "다각도 원인 제시", "score": 3, "maxScore": 3 }, { "item": "정량·정성 근거 신뢰도", "score": 5, "maxScore": 5 }, { "item": "영향·파급력 명시", "score": 3, "maxScore": 3 }, { "item": "핵심 문제 포커싱", "score": 4, "maxScore": 4 }]"
            },
            {
              "sectionType": "FEASIBILITY",
              "gradingListScores": "[{ "item": "로드맵 구체성", "score": 6, "maxScore": 6 }, { "item": "구현체계·자원 현실성", "score": 6, "maxScore": 6 }, { "item": "리스크 식별·대응", "score": 6, "maxScore": 6 }, { "item": "KPI 설계·측정 계획", "score": 6, "maxScore": 6 }, { "item": "경쟁사 분석→차별화 연계", "score": 6, "maxScore": 6 }]"
            },
            {
              "sectionType": "GROWTH_STRATEGY",
              "gradingListScores": "[{ "item": "BM 9요소 완결·연계성", "score": 6, "maxScore": 6 }, { "item": "수익모델·매출 추정", "score": 6, "maxScore": 6 }, { "item": "자금조달·집행 계획", "score": 6, "maxScore": 6 }, { "item": "GTM·채널·전환 지표", "score": 6, "maxScore": 6 }, { "item": "확장 전략", "score": 6, "maxScore": 6 }]"
            },
            {
              "sectionType": "TEAM_COMPETENCE",
              "gradingListScores": "[{ "item": "창업자 전문성·연관성", "score": 5, "maxScore": 5 }, { "item": "팀 밸런스·R&R 명확성", "score": 5, "maxScore": 5 }, { "item": "경력·전공의 적합성", "score": 4, "maxScore": 4 }, { "item": "협업 체계·외부 네트워크", "score": 3, "maxScore": 3 }, { "item": "지속 실행력", "score": 3, "maxScore": 3 }]"
            }
          ]
        }

        - strengths와 weaknesses는 각각 정확히 3개씩 제공해야 합니다.
        - gradingListScores는 각 항목별 점수를 JSON 배열 형태로 제공하세요. 문제인식은 5개 항목, 실현가능성은 5개 항목, 성장전략은 5개 항목, 팀역량은 5개 항목입니다.
        - item 필드에는 위 채점 기준의 각 항목명을 간략하게 담아주세요 (예: "근본 원인 논리 분석", "로드맵 구체성")
        - problemRecognitionScore, feasibilityScore, growthStrategyScore, teamCompetenceScore는 각 섹션의 세부 항목들 점수의 합이므로 이를 꼭 지켜주세요
        """;
  }

  /**
   * 리포트 채점용 사용자 프롬프트 생성
   */
  private String buildReportGradingUserPrompt(String businessPlanContent) {
    return "다음 사업계획서 내용을 채점해주세요:\n\n" + businessPlanContent;
  }

  /**
   * 체크리스트 채점용 시스템 프롬프트
   * 이전 정보가 있으면 이전 정보를 참고하는 프롬프트, 없으면 기본 프롬프트
   */
  private String getChecklistGradingSystemPrompt(boolean hasPrevious) {
    if (hasPrevious) {
      return """
          당신은 JSON 검증기이자 창업 사업계획서 체크리스트 채점 보조자입니다. 사용자 메시지에는 [CHECKLIST], [PREVIOUS_CONTENT], [PREVIOUS_CHECKLIST_RESULT], [NEW_CONTENT], [REQUEST] 섹션이 포함됩니다. 다음을 엄격히 따르세요:

          - 출력은 오직 JSON 배열(Boolean) 하나만 반환합니다. 다른 텍스트, 주석, 키, 객체, 공백, 줄바꿈 금지.
          - true/false 소문자만 사용합니다.
          - 배열 길이는 [REQUEST]에 명시된 길이와 정확히 동일해야 합니다.

          - [NEW_CONTENT]와 [PREVIOUS_CONTENT]를 비교하여 변경사항을 분석하세요.
          - [PREVIOUS_CHECKLIST_RESULT]를 참고하되, [NEW_CONTENT]의 현재 상태를 기준으로 재평가하세요.
          - 이전 내용에서 개선되었거나 새로운 정보가 추가되어 [CHECKLIST] 항목을 만족하게 되었다면 TRUE로 업데이트하세요.
          - 이전 내용과 동일하거나 개선되지 않았다면, [NEW_CONTENT]를 기준으로 재평가하여 적절한 값을 반환하세요.
          - [CHECKLIST] 항목의 순서를 정확히 지켜서 true, false 둘중 하나를 리턴해야야 합니다.

          - 도메인 가이드: TAM/SAM/SOM, SWOT/PEST(STEEP), KPI, 제품/기능 로드맵, 자금 조달/집행(정부지원금·투자·매출·자부담), 시장 진입/확장 전략, 팀 R&R 등 용어를 정확히 해석하세요.

          - 과잉 일반화, 환각, 추측 금지. 명시 근거가 없으면 false입니다.
          - 체크리스트 순서를 바꾸지 마세요.
          """;
    } else {
      return """
          당신은 JSON 검증기이자 창업 사업계획서 체크리스트 채점 보조자입니다. 사용자 메시지에는 [CHECKLIST], [INPUT], [REQUEST] 섹션이 포함됩니다. 다음을 엄격히 따르세요:

          - 출력은 오직 JSON 배열(Boolean) 하나만 반환합니다. 다른 텍스트, 주석, 키, 객체, 공백, 줄바꿈 금지.
          - true/false 소문자만 사용합니다.
          - 배열 길이는 [REQUEST]에 명시된 길이와 정확히 동일해야 합니다.
          - [INPUT]에 대한 내용을 [CHECKLIST] 의 내용으로 순서를 지켜 true, false로 판단해주면 됩니다. 근거 부재 시 false로 판정합니다. 판단은 [CHECKLIST]의 내용에 한정합니다. 해당 내용에 순서를 맞춰 true, false를 반환해주면 됩니다.
          - 도메인 가이드: TAM/SAM/SOM, SWOT/PEST(STEEP), KPI, 제품/기능 로드맵, 자금 조달/집행(정부지원금·투자·매출·자부담), 시장 진입/확장 전략, 팀 R&R 등 용어를 정확히 해석하세요.

          - 과잉 일반화, 환각, 추측 금지. 명시 근거가 없으면 false입니다.
          - 체크리스트 순서를 바꾸지 마세요.
          """;
    }
  }

  /**
   * 체크리스트 채점용 사용자 프롬프트 생성
   * 이전 정보가 있으면 포함하고, 없으면 기본 프롬프트 생성
   */
  private String buildChecklistGradingUserPrompt(
      String newContent,
      List<String> criteria,
      String previousContent,
      List<Boolean> previousChecks) {
    StringBuilder sb = new StringBuilder();
    sb.append("[CHECKLIST]\n");
    for (int i = 0; i < criteria.size(); i++) {
      sb.append(i + 1).append(") ").append(criteria.get(i)).append("\n");
    }

    boolean hasPrevious = previousContent != null && !previousContent.isBlank()
        && previousChecks != null && !previousChecks.isEmpty();

    // 이전 정보가 있으면 추가
    if (hasPrevious) {
      sb.append("\n[PREVIOUS_CONTENT]\n").append(previousContent).append("\n");
      sb.append("\n[PREVIOUS_CHECKLIST_RESULT]\n");
      for (int i = 0; i < previousChecks.size() && i < criteria.size(); i++) {
        sb.append(i + 1).append(") ").append(previousChecks.get(i) ? "TRUE" : "FALSE").append("\n");
      }
      sb.append("\n[NEW_CONTENT]\n").append(newContent).append("\n\n");
      sb.append("[REQUEST]\n");
      sb.append("위의 [NEW_CONTENT]를 [PREVIOUS_CONTENT]와 비교하여 변경사항을 확인하고, ")
          .append("[PREVIOUS_CHECKLIST_RESULT]를 참고하여 [CHECKLIST] 항목 각각에 대해 업데이트된 판단을 내려주세요. ")
          .append("이전 내용에서 개선되었거나 추가된 부분이 있으면 해당 항목을 TRUE로, ")
          .append("이전 내용과 동일하거나 개선되지 않은 부분은 이전 결과를 유지하되, ")
          .append("새로운 내용을 기준으로 재평가하여 최종 판단해주세요. ")
          .append("최종 출력은 길이 ").append(criteria.size()).append("의 JSON 배열(Boolean)로만 반환하세요.");
    } else {
      sb.append("\n[INPUT]\n").append(newContent).append("\n\n");
      sb.append("[REQUEST]\n");
      sb.append("위의 [INPUT]을 [CHECKLIST] 항목 각각에 대해 판단하여 TRUE/FALSE로만 판단하되, 최종 출력은 길이 ")
          .append(criteria.size()).append("의 JSON 배열(Boolean)로만 반환");
    }

    return sb.toString();
  }
}
