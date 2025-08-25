package web.car_system.Car_Service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptionDto {
    private String value; // Giá trị sẽ được lưu vào DB, ví dụ: "FWD"
    private String label; // Nhãn sẽ được hiển thị cho người dùng, ví dụ: "Cầu trước (FWD)"
}