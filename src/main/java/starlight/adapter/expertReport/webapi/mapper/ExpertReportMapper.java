package starlight.adapter.expertReport.webapi.mapper;

import org.springframework.stereotype.Component;
import starlight.adapter.expertReport.webapi.dto.CreateExpertReportDetailRequest;
import starlight.domain.expertReport.entity.ExpertReportDetail;

import java.util.List;

@Component
public class ExpertReportMapper {
    public ExpertReportDetail toEntity(CreateExpertReportDetailRequest dto) {
        return ExpertReportDetail.create(
                dto.commentType(),
                dto.content()
        );
    }

    public List<ExpertReportDetail> toEntityList(List<CreateExpertReportDetailRequest> dtos) {
        return dtos.stream()
                .map(this::toEntity)
                .toList();
    }
}
