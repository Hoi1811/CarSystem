package web.car_system.Car_Service.service.impl;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import web.car_system.Car_Service.domain.dto.audit.AuditLogDTO;
import web.car_system.Car_Service.domain.dto.audit.RevisionDTO;
import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.domain.entity.CustomRevisionEntity;
import web.car_system.Car_Service.service.AuditService;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {
    private final EntityManager entityManager;

    // Phương thức helper để lấy AuditReader
    private AuditReader getAuditReader() {
        return AuditReaderFactory.get(this.entityManager);
    }

    @Override
    public List<AuditLogDTO<Car>> getCarHistory(Integer carId) {
        AuditReader auditReader = getAuditReader();

        // Tạo một câu query để lấy tất cả các phiên bản của Car.class
        List<Object[]> results = auditReader.createQuery()
                .forRevisionsOfEntity(Car.class, true, true)
                .add(AuditEntity.id().eq(carId)) // Lọc theo carId
                .addOrder(AuditEntity.revisionNumber().asc()) // Sắp xếp theo thứ tự phiên bản
                .getResultList();

        // Chuyển đổi kết quả thô (List<Object[]>) sang List<AuditLogDTO>
        return results.stream()
                .map(resultArray -> {
                    Car car = (Car) resultArray[0];
                    CustomRevisionEntity revisionEntity = (CustomRevisionEntity) resultArray[1];
                    RevisionType revisionType = (RevisionType) resultArray[2];
                    Date revisionDate = new Date(revisionEntity.getTimestamp());
                    RevisionDTO revisionDTO = new RevisionDTO(
                            revisionEntity.getId(),
                            revisionDate,
                            revisionEntity.getUsername()
                    );

                    return new AuditLogDTO<>(car, revisionDTO, revisionType);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Car getCarAtRevision(Integer carId, Integer revisionNumber) {
        AuditReader auditReader = getAuditReader();
        return auditReader.find(Car.class, carId, revisionNumber);
    }
}
