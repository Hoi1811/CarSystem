package web.car_system.Car_Service.domain.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class RevisionDTO {
    private int id;
    private Date timestamp;
    private String username;
}