package starlight.adapter.shared.webapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.net.URI;

public class ValidPdfUrlValidator implements ConstraintValidator<ValidPdfUrl, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        try {
            URI.create(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
