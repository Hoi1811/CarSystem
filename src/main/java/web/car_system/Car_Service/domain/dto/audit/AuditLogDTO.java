package web.car_system.Car_Service.domain.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.envers.RevisionType;

@Data
@AllArgsConstructor
public class AuditLogDTO<T> {
    private T entity;
    private RevisionDTO revision;
    private RevisionType revisionType; // loại thay đổi: ADD, MOD, DEL
}