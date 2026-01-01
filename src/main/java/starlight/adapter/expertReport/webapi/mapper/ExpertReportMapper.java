package starlight.adapter.expertReport.webapi.mapper;

import org.springframework.stereotype.Component;
import starlight.adapter.expertReport.webapi.dto.CreateExpertReportCommentRequest;
import starlight.domain.expertReport.entity.ExpertReportComment;

import java.util.List;

@Component
public class ExpertReportMapper {
    public ExpertReportComment toEntity(CreateExpertReportCommentRequest dto) {
        return ExpertReportComment.create(
                dto.type(),
                dto.content()
        );
    }

    public List<ExpertReportComment> toEntityList(List<CreateExpertReportCommentRequest> dtos) {
        return dtos.stream()
                .map(this::toEntity)
                .toList();
    }
}
