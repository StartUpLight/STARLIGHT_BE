package starlight.application.backoffice.member.provided.dto.result;

import java.util.List;

public record BackofficeUserDashboardResult(
        long totalUsers,
        long activeUsers,
        long newUsersThisMonth,
        long churnedUsersThisMonth,
        List<MonthlySignupResult> monthlySignups,
        List<SignupChannelResult> signupChannels
) {
    public static BackofficeUserDashboardResult of(
            long totalUsers,
            long activeUsers,
            long newUsersThisMonth,
            long churnedUsersThisMonth,
            List<MonthlySignupResult> monthlySignups,
            List<SignupChannelResult> signupChannels
    ) {
        return new BackofficeUserDashboardResult(
                totalUsers,
                activeUsers,
                newUsersThisMonth,
                churnedUsersThisMonth,
                monthlySignups,
                signupChannels
        );
    }

    public record MonthlySignupResult(
            String month,
            long value
    ) {
        public static MonthlySignupResult of(String month, long value) {
            return new MonthlySignupResult(month, value);
        }
    }

    public record SignupChannelResult(
            String provider,
            long value,
            double growthRate
    ) {
        public static SignupChannelResult of(String provider, long value, double growthRate) {
            return new SignupChannelResult(provider, value, growthRate);
        }
    }
}
