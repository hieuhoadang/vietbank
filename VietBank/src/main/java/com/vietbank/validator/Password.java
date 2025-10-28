package com.vietbank.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidatorImpl.class)
@Documented
public @interface Password {
	
	String message() default "{error.invalid.password}";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}

class PasswordValidatorImpl implements ConstraintValidator<Password, String>
{
	// Một ký tự 
	private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,}$");
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if(value == null) return true;
		return PASSWORD_PATTERN.matcher(value).matches();
	}
	
}
