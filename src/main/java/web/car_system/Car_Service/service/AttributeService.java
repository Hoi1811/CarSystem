package web.car_system.Car_Service.service;

import web.car_system.Car_Service.domain.entity.Attribute;

public interface AttributeService {
    Attribute createAttribute(Integer specificationId, String name);
    Attribute updateAttribute(Integer id, String name);
    void deleteAttribute(Integer id);
}
