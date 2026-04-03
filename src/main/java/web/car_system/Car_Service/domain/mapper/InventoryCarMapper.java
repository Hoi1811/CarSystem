package web.car_system.Car_Service.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mappings;
import web.car_system.Car_Service.domain.dto.inventory_car.CreateInventoryCarRequest;
import web.car_system.Car_Service.domain.dto.inventory_car.InventoryCarDto;
import web.car_system.Car_Service.domain.entity.InventoryCar;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InventoryCarMapper {
    /**
     * Chuyển đổi từ Entity (InventoryCar) sang DTO (InventoryCarDto).
     * MapStruct sẽ tự động map các trường có tên giống nhau.
     * Chúng ta chỉ cần chỉ định các trường có tên khác hoặc cần lấy từ các đối tượng lồng nhau.
     */
    @Mappings({
            // Lấy thông tin từ đối tượng 'car' lồng trong InventoryCar
            @Mapping(source = "car.carId", target = "carId"),
            @Mapping(source = "car.name", target = "carName"),
            @Mapping(source = "car.model", target = "carModel"),
            @Mapping(source = "car.year", target = "carYear"),
            @Mapping(source = "car.thumbnail", target = "carThumbnail"),
            // Lấy thông tin từ 'manufacturer' lồng trong 'car'
            @Mapping(source = "car.manufacturer.name", target = "manufacturerName"),
            // Lấy thông tin từ 'showroom' lồng trong InventoryCar
            @Mapping(source = "showroom.id", target = "showroomId"),
            @Mapping(source = "showroom.name", target = "showroomName"),
            @Mapping(source = "showroom.code", target = "showroomCode"),
            @Mapping(source = "showroom.address", target = "showroomAddress"),
            @Mapping(source = "showroom.phone", target = "showroomPhone"),
            @Mapping(source = "showroom.latitude", target = "showroomLatitude"),
            @Mapping(source = "showroom.longitude", target = "showroomLongitude")
    })
    InventoryCarDto toDto(InventoryCar inventoryCar);

    /**
     * Chuyển đổi từ Request DTO (CreateInventoryCarRequest) sang Entity (InventoryCar).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "showroom", ignore = true) // Sẽ được gán thủ công trong service
    InventoryCar toEntity(CreateInventoryCarRequest request);
}
