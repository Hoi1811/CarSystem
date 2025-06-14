package web.car_system.Car_Service.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import web.car_system.Car_Service.domain.entity.Attribute;
import web.car_system.Car_Service.domain.entity.Specification;
import web.car_system.Car_Service.repository.AttributeRepository;
import web.car_system.Car_Service.repository.SpecificationRepository;
import web.car_system.Car_Service.service.AttributeService;
@Service
@RequiredArgsConstructor
public class AttributeServiceImpl implements AttributeService {
    private final AttributeRepository attributeRepository;
    private final SpecificationRepository specificationRepository;
    @Override
    public Attribute createAttribute(Integer specificationId, String name) {
        Specification specification = specificationRepository.findById(specificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Specification not found"));
        Attribute attribute = new Attribute();
        attribute.setSpecification(specification);
        attribute.setName(name);
        return attributeRepository.save(attribute);
    }
    @Transactional
    @Override
    public Attribute updateAttribute(Integer id, String name) {
        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attribute not found"));
        attribute.setName(name);
        return attributeRepository.save(attribute);
    }
    @Transactional
    @Override
    public void deleteAttribute(Integer id) {
        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attribute not found"));
        attributeRepository.delete(attribute);
    }
}
