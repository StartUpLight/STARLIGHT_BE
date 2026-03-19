package starlight.adapter.shared.webapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class ValidFileNameValidator implements ConstraintValidator<ValidFileName, String> {

    private static final String IMAGE_EXTENSIONS = "png|jpg|jpeg|webp";
    private static final String IMAGE_AND_PDF_EXTENSIONS = IMAGE_EXTENSIONS + "|pdf";

    private Pattern fileNamePattern;

    @Override
    public void initialize(ValidFileName annotation) {
        String extensions = annotation.imageOnly() ? IMAGE_EXTENSIONS : IMAGE_AND_PDF_EXTENSIONS;
        this.fileNamePattern =
                Pattern.compile("^[A-Za-z0-9._-]+\\.(" + extensions + ")$", Pattern.CASE_INSENSITIVE);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (!StringUtils.hasText(value)) {
            return false;
        }

        if (value.contains("/") || value.contains("\\")) {
            return false;
        }

        return fileNamePattern.matcher(value).matches();
    }
}
