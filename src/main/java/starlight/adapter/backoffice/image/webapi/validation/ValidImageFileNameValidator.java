package starlight.adapter.backoffice.image.webapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class ValidImageFileNameValidator implements ConstraintValidator<ValidImageFileName, String> {

    private static final Pattern FILE_NAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9._-]+\\.(png|jpg|jpeg|webp)$", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (!StringUtils.hasText(value)) {
            return false;
        }

        if (value.contains("/") || value.contains("\\")) {
            return false;
        }

        return FILE_NAME_PATTERN.matcher(value).matches();
    }
}
