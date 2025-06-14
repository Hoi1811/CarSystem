package web.car_system.Car_Service.utility;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import web.car_system.Car_Service.domain.entity.Attribute;
import web.car_system.Car_Service.domain.entity.Specification;
import web.car_system.Car_Service.repository.AttributeRepository;
import web.car_system.Car_Service.repository.SpecificationRepository;

import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class CarDataCache {

    private final SpecificationRepository specificationRepository;

    private final AttributeRepository attributeRepository;

    @Cacheable("specifications")
    public Map<String, Specification> getSpecificationMap() {
        Map<String, Specification> map = new HashMap<>();
        specificationRepository.findAll().forEach(spec -> map.put(spec.getName(), spec));
        return map;
    }

    @Cacheable("attributes")
    public Map<String, Attribute> getAttributeMap() {
        Map<String, Attribute> map = new HashMap<>();
        attributeRepository.findAll().forEach(attr ->
                map.put(attr.getName() + "_" + attr.getSpecification().getSpecificationId(), attr));
        return map;
    }

    // Tạo mới Specification và Attribute nếu không có trong cache
    public Specification addSpecification(String name) {
        Specification spec = new Specification();
        spec.setName(name);
        return specificationRepository.save(spec);
    }

    public Attribute addAttribute(String name, Specification specification) {
        Attribute attr = new Attribute();
        attr.setName(name);
        attr.setSpecification(specification);
        return attributeRepository.save(attr);
    }
}
