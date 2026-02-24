package starlight.adapter.backoffice.member.webapi.dto.response;

import starlight.application.backoffice.member.provided.dto.result.BackofficeUserPageResult;

import java.util.List;

public record BackofficeUserPageResponse(
        List<BackofficeUserRowResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static BackofficeUserPageResponse from(BackofficeUserPageResult result) {
        return new BackofficeUserPageResponse(
                result.content().stream()
                        .map(BackofficeUserRowResponse::from)
                        .toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext()
        );
    }
}
