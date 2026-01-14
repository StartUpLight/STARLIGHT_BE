package starlight.adapter.businessplan.creation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import starlight.application.businessplan.provided.BusinessPlanUseCase;
import starlight.application.businessplan.provided.dto.BusinessPlanResult;
import starlight.application.aireport.required.BusinessPlanCreationPort;

/**
 * BusinessPlanCreationPort의 구현체
 * BusinessPlanUseCase를 래핑하여 필요한 기능만 노출합니다.
 */
@Component
@RequiredArgsConstructor
public class BusinessPlanCreationAdapter implements BusinessPlanCreationPort {
    
    private final BusinessPlanUseCase businessPlanUseCase;
    
    @Override
    public Long createBusinessPlanWithPdf(String title, String pdfUrl, Long memberId) {
        BusinessPlanResult.Result result = businessPlanUseCase.createBusinessPlanWithPdf(
                title,
                pdfUrl,
                memberId
        );
        return result.businessPlanId();
    }
}

