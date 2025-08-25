package web.car_system.Car_Service.domain.dto.car;

import web.car_system.Car_Service.domain.entity.EntityStatus;

public record UpdateCarStatusRequestDTO(EntityStatus entityStatus) {
}
