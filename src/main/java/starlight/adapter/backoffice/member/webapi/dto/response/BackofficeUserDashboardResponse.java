package starlight.adapter.backoffice.member.webapi.dto.response;

import starlight.application.backoffice.member.provided.dto.result.BackofficeUserDashboardResult;

import java.util.List;

public record BackofficeUserDashboardResponse(
        long totalUsers,
        long activeUsers,
        long newUsersThisMonth,
        long churnedUsersThisMonth,
        List<MonthlySignupResponse> monthlySignups,
        List<SignupChannelResponse> signupChannels
) {
    public static BackofficeUserDashboardResponse from(BackofficeUserDashboardResult result) {
        return new BackofficeUserDashboardResponse(
                result.totalUsers(),
                result.activeUsers(),
                result.newUsersThisMonth(),
                result.churnedUsersThisMonth(),
                result.monthlySignups().stream()
                        .map(MonthlySignupResponse::from)
                        .toList(),
                result.signupChannels().stream()
                        .map(SignupChannelResponse::from)
                        .toList()
        );
    }

    public record MonthlySignupResponse(
            String month,
            long value
    ) {
        public static MonthlySignupResponse from(BackofficeUserDashboardResult.MonthlySignupResult result) {
            return new MonthlySignupResponse(result.month(), result.value());
        }
    }

    public record SignupChannelResponse(
            String provider,
            long value,
            double growthRate
    ) {
        public static SignupChannelResponse from(BackofficeUserDashboardResult.SignupChannelResult result) {
            return new SignupChannelResponse(result.provider(), result.value(), result.growthRate());
        }
    }
}
