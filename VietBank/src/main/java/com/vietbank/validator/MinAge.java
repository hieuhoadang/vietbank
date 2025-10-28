package com.vietbank.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AgeValidatorImpl.class)
@Documented
public @interface MinAge {
	String message() default "{min.age.message}";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
	int value();
}

class AgeValidatorImpl implements ConstraintValidator<MinAge, LocalDate> {
    private int minAge;

    @Override
    public void initialize(MinAge constraintAnnotation) {
        this.minAge = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(LocalDate dateOfBirth, ConstraintValidatorContext context) {
    	if (Objects.isNull(dateOfBirth)) return true;
    	long years = ChronoUnit.YEARS.between(dateOfBirth, LocalDate.now());

        return years >= minAge;
    }
}
