package web.car_system.Car_Service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import web.car_system.Car_Service.domain.dto.global.FilterCarPaginationRequestDTO;

public class PriceRangeValidator implements ConstraintValidator<PriceRangeValid, FilterCarPaginationRequestDTO> {

    @Override
    public void initialize(PriceRangeValid constraintAnnotation) {
    }

    @Override
    public boolean isValid(FilterCarPaginationRequestDTO dto, ConstraintValidatorContext context) {
        if (dto.minPrice() == null || dto.maxPrice() == null) {
            return true; // Nếu một trong hai là null, không validate ở đây, để @DecimalMin xử lý
        }
        boolean isValid = dto.maxPrice().compareTo(dto.minPrice()) >= 0;
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("maxPrice") // Gán lỗi cho trường maxPrice
                    .addConstraintViolation();
        }
        return isValid;
    }
}