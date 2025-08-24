package web.car_system.Car_Service.service;

import web.car_system.Car_Service.domain.dto.attribute.AttributeRequestDTO;
import web.car_system.Car_Service.domain.dto.attribute.AttributeResponseDTO;
import web.car_system.Car_Service.domain.dto.attribute_management.EnumOrderRequestDTO;
import web.car_system.Car_Service.domain.dto.attribute_management.EnumOrderResponseDTO;
import web.car_system.Car_Service.domain.entity.Attribute;

import java.util.List;

public interface AttributeService {

    AttributeResponseDTO createAttribute(AttributeRequestDTO requestDTO);

    AttributeResponseDTO getAttributeById(Integer id);

    List<AttributeResponseDTO> getAllAttributes();

    AttributeResponseDTO updateAttribute(Integer id, AttributeRequestDTO requestDTO);

    void deleteAttribute(Integer id);

    EnumOrderResponseDTO addOptionToAttribute(Integer attributeId, EnumOrderRequestDTO requestDTO);
    List<EnumOrderResponseDTO> getOptionsForAttribute(Integer attributeId);
    EnumOrderResponseDTO updateOptionForAttribute(Integer attributeId, String valueKey, EnumOrderRequestDTO requestDTO);
    void deleteOptionFromAttribute(Integer attributeId, String valueKey);
}