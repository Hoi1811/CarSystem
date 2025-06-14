package web.car_system.Car_Service.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import web.car_system.Car_Service.domain.dto.car.AddCarRequestDTO;
import web.car_system.Car_Service.domain.dto.attribute.AttributeResponseDTO;
import web.car_system.Car_Service.domain.dto.car.CarDetailsResponseDTO;
import web.car_system.Car_Service.domain.dto.car.CarResponseDTO;
import web.car_system.Car_Service.domain.dto.car.UpdateCarRequestDTO;
import web.car_system.Car_Service.domain.dto.specification.SpecificationResponseDTO;
import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.domain.entity.CarAttribute;
import web.car_system.Car_Service.domain.entity.CarType;
import web.car_system.Car_Service.domain.entity.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface CarMapper {
    CarMapper INSTANCE = Mappers.getMapper(CarMapper.class);


    // 1. Chuyển từ CarRequest sang Car entity
    @Mapping(target = "carId", ignore = true) // ID sinh tự động
    @Mapping(target = "carAttributes", ignore = true) // Xử lý riêng trong service
    @Mapping(target = "carTypes", ignore = true) // Không dùng trong JSON
    @Mapping(target = "createdAt", ignore = true) // Sinh tự động bởi database
    @Mapping(target = "thumbnail", ignore = true)
    @Mapping(target = "carSegment", ignore = true)
    @Mapping(target = "manufacturer", ignore = true)
    @Mapping(target = "origin", ignore = true) // Không dùng trong JSON
    Car toCar(AddCarRequestDTO addCarRequestDTO);

    @Mapping(target = "carId", ignore = true) // ID sinh tự động
    @Mapping(target = "carAttributes", ignore = true) // Xử lý riêng trong service
    @Mapping(target = "carTypes", ignore = true) // Không dùng trong JSON
    @Mapping(target = "createdAt", ignore = true) // Sinh tự động bởi database
    @Mapping(target = "thumbnail", ignore = true)
    @Mapping(target = "carSegment", ignore = true)
    @Mapping(target = "manufacturer", ignore = true)
    @Mapping(target = "origin", ignore = true) // Không dùng trong JSON
    Car toCar(UpdateCarRequestDTO updateCarRequestDTO);

    @Mapping(target = "carId", ignore = true) // Bỏ qua các trường không cần cập nhật
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "thumbnail", ignore = true)
    void updateCarFromDto(UpdateCarRequestDTO dto, @MappingTarget Car car);

    // 2. Chuyển từ Car entity sang CarResponse
    @Mapping(source = "carAttributes", target = "specifications", qualifiedByName = "mapCarAttributesToSpecifications")
    @Mapping(source = "carTypes", target = "carTypeIds", qualifiedByName = "mapCarTypesToIds") // Chuyển carTypes thành danh sách ID
    CarDetailsResponseDTO toCarDetailsResponse(Car car);

//    @Mapping(target = "carAttributes", ignore = true) // Không dùng trong JSON
    @Mapping(source = "carTypes", target = "carTypeIds", qualifiedByName = "mapCarTypesToIds") // Chuyển carTypes thành danh sách ID
    CarResponseDTO toCarResponseDTO(Car car);

    // 3. Chuyển từ CarAttribute sang AttributeResponse
    @Mapping(source = "attribute.attributeId", target = "id") // Lấy id từ Attribute
    @Mapping(source = "attribute.name", target = "name") // Chỉ lấy name từ Attribute
    @Mapping(source = "value", target = "value") // Lấy value từ CarAttribute
    AttributeResponseDTO toAttributeResponse(CarAttribute carAttribute);

    // 4. Nhóm CarAttributes thành danh sách SpecificationResponse
    @Named("mapCarAttributesToSpecifications")
    default List<SpecificationResponseDTO> mapCarAttributesToSpecifications(List<CarAttribute> carAttributes) {
        if (carAttributes == null || carAttributes.isEmpty()) {
            return List.of(); // Trả về danh sách rỗng nếu không có attributes
        }

        // Nhóm CarAttributes theo Specification
        Map<Specification, List<CarAttribute>> groupedBySpec = carAttributes.stream()
                .collect(Collectors.groupingBy(ca -> ca.getAttribute().getSpecification()));

        // Chuyển từng nhóm thành SpecificationResponse (không bao gồm specificationId)
        return groupedBySpec.entrySet().stream()
                .map(entry -> {
                    web.car_system.Car_Service.domain.entity.Specification spec = entry.getKey();
                    List<AttributeResponseDTO> attrResponses = entry.getValue().stream()
                            .map(this::toAttributeResponse)
                            .collect(Collectors.toList());
                    return new SpecificationResponseDTO(spec.getSpecificationId(), spec.getName(), attrResponses);
                })
                .collect(Collectors.toList());
    }
    @Named("mapCarTypesToIds")
    default List<Integer> mapCarTypesToIds(List<CarType> carTypes) {
        if (carTypes == null) {
            return Collections.emptyList(); // Hoặc new ArrayList<>()
        }
        return carTypes.stream()
                .map(CarType::getTypeId)
                .collect(Collectors.toList());
    }

}
