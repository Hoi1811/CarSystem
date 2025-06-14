package web.car_system.Car_Service.domain.dto.global;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record GlobalResponseDTO<Meta, Data>(
    Meta meta,
    Data data
) {

}
