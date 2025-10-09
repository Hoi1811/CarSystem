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
            @Mapping(source = "car.manufacturer.name", target = "manufacturerName")
    })
    InventoryCarDto toDto(InventoryCar inventoryCar);

    /**
     * Chuyển đổi từ Request DTO (CreateInventoryCarRequest) sang Entity (InventoryCar).
     * Chức năng này sẽ được dùng trong cả phương thức create và update.
     * Lưu ý: Chúng ta sẽ không map carId vì nó cần được xử lý riêng.
     */
    @Mapping(target = "id", ignore = true) // Bỏ qua id khi tạo mới
    @Mapping(target = "car", ignore = true) // Bỏ qua car vì sẽ được gán thủ công
    InventoryCar toEntity(CreateInventoryCarRequest request);
}
