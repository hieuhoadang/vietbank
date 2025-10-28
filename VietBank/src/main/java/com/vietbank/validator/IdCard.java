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
@Constraint(validatedBy = IdCardValidatorImpl.class)
@Documented
public @interface IdCard {
	
	String message() default "{error.invalid.idcard}";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}

class IdCardValidatorImpl implements ConstraintValidator<IdCard, String>
{
	private static final Pattern IDCARD_PATTERN = Pattern.compile("^\\d{9,12}$");
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if(value ==  null) return true;
		
		return IDCARD_PATTERN.matcher(value).matches();
	}
	
}
