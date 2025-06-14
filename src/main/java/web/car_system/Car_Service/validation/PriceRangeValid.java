package web.car_system.Car_Service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = PriceRangeValidator.class)
@Target({ ElementType.TYPE }) // Áp dụng cho class level
@Retention(RetentionPolicy.RUNTIME)
public @interface PriceRangeValid {
    String message() default "Maximum price must be greater than or equal to minimum price.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}