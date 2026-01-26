package starlight.application.backoffice.mail.util;

import java.util.List;
import java.util.stream.Collectors;

public final class EmailMaskingUtils {

    private EmailMaskingUtils() {
    }

    public static String maskRecipients(List<String> recipients) {
        if (recipients == null || recipients.isEmpty()) {
            return "";
        }
        return recipients.stream()
                .map(EmailMaskingUtils::maskEmail)
                .collect(Collectors.joining(","));
    }

    private static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "***";
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 0) {
            return "***";
        }
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1);
        String maskedLocal = local.length() <= 1 ? "*" : local.charAt(0) + "***";
        String maskedDomain = domain.isBlank() ? "***" : domain;
        return maskedLocal + "@" + maskedDomain;
    }
}
