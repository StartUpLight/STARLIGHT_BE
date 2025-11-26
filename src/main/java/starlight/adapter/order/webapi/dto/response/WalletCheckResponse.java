package starlight.adapter.order.webapi.dto.response;

public record WalletCheckResponse(
        Boolean hasCredit
) {
    public static WalletCheckResponse of(Boolean hasCredit) {
        return new WalletCheckResponse(
                hasCredit
        );
    }
}