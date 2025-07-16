package web.car_system.Car_Service.service;

import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import web.car_system.Car_Service.domain.dto.audit.AuditLogDTO;
import web.car_system.Car_Service.domain.entity.Car;

import java.util.List;

public interface AuditService {
    List<AuditLogDTO<Car>> getCarHistory(Integer carId);
    Car getCarAtRevision(Integer carId, Integer revisionNumber);


}
