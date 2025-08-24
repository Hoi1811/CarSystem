package web.car_system.Car_Service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.constant.Endpoint;
import web.car_system.Car_Service.domain.dto.attribute.AttributeRequestDTO;
import web.car_system.Car_Service.domain.dto.attribute.AttributeResponseDTO;
import web.car_system.Car_Service.domain.dto.attribute_management.EnumOrderRequestDTO;
import web.car_system.Car_Service.domain.dto.attribute_management.EnumOrderResponseDTO;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.Status;
import web.car_system.Car_Service.service.AttributeService;

import java.util.List;

@RestApiV1
@RequiredArgsConstructor
public class AttributeController {

    private final AttributeService attributeService;

    // --- CRUD for Attribute ---

    @PostMapping(Endpoint.V1.ATTRIBUTE.CREATE_ATTRIBUTE)
    public ResponseEntity<GlobalResponseDTO<?, AttributeResponseDTO>> createAttribute(
            @Valid @RequestBody AttributeRequestDTO requestDTO) {
        AttributeResponseDTO newAttribute = attributeService.createAttribute(requestDTO);

        GlobalResponseDTO<?, AttributeResponseDTO> response = GlobalResponseDTO.<NoPaginatedMeta, AttributeResponseDTO>builder()
                .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message("Tạo thuộc tính thành công.").build())
                .data(newAttribute)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping(Endpoint.V1.ATTRIBUTE.GET_ALL_ATTRIBUTES)
    public ResponseEntity<GlobalResponseDTO<?, List<AttributeResponseDTO>>> getAllAttributes() {
        List<AttributeResponseDTO> attributes = attributeService.getAllAttributes();

        GlobalResponseDTO<?, List<AttributeResponseDTO>> response = GlobalResponseDTO.<NoPaginatedMeta, List<AttributeResponseDTO>>builder()
                .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message("Lấy danh sách thuộc tính thành công.").build())
                .data(attributes)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping(Endpoint.V1.ATTRIBUTE.GET_ATTRIBUTE_BY_ID)
    public ResponseEntity<GlobalResponseDTO<?, AttributeResponseDTO>> getAttributeById(@PathVariable Integer id) {
        AttributeResponseDTO attribute = attributeService.getAttributeById(id);

        GlobalResponseDTO<?, AttributeResponseDTO> response = GlobalResponseDTO.<NoPaginatedMeta, AttributeResponseDTO>builder()
                .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message("Lấy chi tiết thuộc tính thành công.").build())
                .data(attribute)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping(Endpoint.V1.ATTRIBUTE.UPDATE_ATTRIBUTE)
    public ResponseEntity<GlobalResponseDTO<?, AttributeResponseDTO>> updateAttribute(
            @PathVariable Integer id,
            @Valid @RequestBody AttributeRequestDTO requestDTO) {
        AttributeResponseDTO updatedAttribute = attributeService.updateAttribute(id, requestDTO);

        GlobalResponseDTO<?, AttributeResponseDTO> response = GlobalResponseDTO.<NoPaginatedMeta, AttributeResponseDTO>builder()
                .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message("Cập nhật thuộc tính thành công.").build())
                .data(updatedAttribute)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping(Endpoint.V1.ATTRIBUTE.DELETE_ATTRIBUTE)
    public ResponseEntity<GlobalResponseDTO<?, Void>> deleteAttribute(@PathVariable Integer id) {
        attributeService.deleteAttribute(id);

        GlobalResponseDTO<?, Void> response = GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message("Xóa thuộc tính thành công.").build())
                .build();

        return ResponseEntity.ok(response);
    }

    // --- CRUD for Attribute Options (EnumOrder) ---

    @PostMapping(Endpoint.V1.ATTRIBUTE.ADD_OPTION_TO_ATTRIBUTE)
    public ResponseEntity<GlobalResponseDTO<?, EnumOrderResponseDTO>> addOptionToAttribute(
            @PathVariable("attributeId") Integer attributeId,
            @Valid @RequestBody EnumOrderRequestDTO requestDTO) {
        EnumOrderResponseDTO newOption = attributeService.addOptionToAttribute(attributeId, requestDTO);

        GlobalResponseDTO<?, EnumOrderResponseDTO> response = GlobalResponseDTO.<NoPaginatedMeta, EnumOrderResponseDTO>builder()
                .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message("Thêm tùy chọn mới thành công.").build())
                .data(newOption)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping(Endpoint.V1.ATTRIBUTE.GET_OPTIONS_FOR_ATTRIBUTE)
    public ResponseEntity<GlobalResponseDTO<?, List<EnumOrderResponseDTO>>> getOptionsForAttribute(
            @PathVariable("attributeId") Integer attributeId) {
        List<EnumOrderResponseDTO> options = attributeService.getOptionsForAttribute(attributeId);

        GlobalResponseDTO<?, List<EnumOrderResponseDTO>> response = GlobalResponseDTO.<NoPaginatedMeta, List<EnumOrderResponseDTO>>builder()
                .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message("Lấy danh sách tùy chọn thành công.").build())
                .data(options)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping(Endpoint.V1.ATTRIBUTE.UPDATE_OPTION_FOR_ATTRIBUTE)
    public ResponseEntity<GlobalResponseDTO<?, EnumOrderResponseDTO>> updateOptionForAttribute(
            @PathVariable("attributeId") Integer attributeId,
            @PathVariable("valueKey") String valueKey,
            @Valid @RequestBody EnumOrderRequestDTO requestDTO) {
        EnumOrderResponseDTO updatedOption = attributeService.updateOptionForAttribute(attributeId, valueKey, requestDTO);

        GlobalResponseDTO<?, EnumOrderResponseDTO> response = GlobalResponseDTO.<NoPaginatedMeta, EnumOrderResponseDTO>builder()
                .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message("Cập nhật tùy chọn thành công.").build())
                .data(updatedOption)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping(Endpoint.V1.ATTRIBUTE.DELETE_OPTION_FROM_ATTRIBUTE)
    public ResponseEntity<GlobalResponseDTO<?, Void>> deleteOptionFromAttribute(
            @PathVariable("attributeId") Integer attributeId,
            @PathVariable("valueKey") String valueKey) {
        attributeService.deleteOptionFromAttribute(attributeId, valueKey);

        GlobalResponseDTO<?, Void> response = GlobalResponseDTO.<NoPaginatedMeta, Void>builder()
                .meta(NoPaginatedMeta.builder().status(Status.SUCCESS).message("Xóa tùy chọn thành công.").build())
                .build();

        return ResponseEntity.ok(response);
    }
}