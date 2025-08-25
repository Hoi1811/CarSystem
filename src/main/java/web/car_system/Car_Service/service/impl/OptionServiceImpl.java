package web.car_system.Car_Service.service.impl;

import org.springframework.stereotype.Service;
import web.car_system.Car_Service.domain.dto.OptionDto;
import web.car_system.Car_Service.service.OptionService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class OptionServiceImpl implements OptionService {
    @Override
    public List<OptionDto> getOptionsBySourceName(String sourceName) {
        if (sourceName == null) {
            return Collections.emptyList();
        }

        switch (sourceName.toLowerCase()) {
            case "drive_trains":
                return getDriveTrainOptions();
            case "gearboxes":
                return getGearboxOptions();
            case "fuel_types":
                return getFuelTypeOptions();
            // Bạn có thể thêm các case khác sau này, ví dụ: 'seat_materials'
            default:
                return Collections.emptyList(); // Trả về rỗng nếu không tìm thấy nguồn
        }
    }
    // ----- Các phương thức private để cung cấp dữ liệu -----

    private List<OptionDto> getDriveTrainOptions() {
        return Arrays.asList(
                new OptionDto("FWD", "Cầu trước (FWD)"),
                new OptionDto("RWD", "Cầu sau (RWD)"),
                new OptionDto("AWD", "4 bánh toàn thời gian (AWD)"),
                new OptionDto("4WD", "Dẫn động 2 cầu / Gài cầu (4WD)")
        );
    }

    private List<OptionDto> getGearboxOptions() {
        // Lấy các giá trị phổ biến nhất từ việc khảo sát
        return Arrays.asList(
                new OptionDto("AT", "Tự động (AT)"),
                new OptionDto("MT", "Số sàn (MT)"),
                new OptionDto("CVT", "Vô cấp (CVT)"),
                new OptionDto("DCT", "Ly hợp kép (DCT)"),
                new OptionDto("SINGLE_SPEED", "Đơn cấp (Xe điện)"),
                // Các biến thể có cấp số
                new OptionDto("4AT", "Tự động 4 cấp"),
                new OptionDto("5AT", "Tự động 5 cấp"),
                new OptionDto("6AT", "Tự động 6 cấp"),
                new OptionDto("7AT", "Tự động 7 cấp"),
                new OptionDto("8AT", "Tự động 8 cấp"),
                new OptionDto("9AT", "Tự động 9 cấp"),
                new OptionDto("10AT", "Tự động 10 cấp"),
                new OptionDto("5MT", "Số sàn 5 cấp"),
                new OptionDto("6MT", "Số sàn 6 cấp"),
                new OptionDto("7DCT", "Ly hợp kép 7 cấp"),
                new OptionDto("8DCT", "Ly hợp kép 8 cấp")
        );
    }

    private List<OptionDto> getFuelTypeOptions() {
        return Arrays.asList(
                new OptionDto("GASOLINE", "Xăng"),
                new OptionDto("DIESEL", "Dầu (Diesel)"),
                new OptionDto("ELECTRIC", "Điện"),
                new OptionDto("HYBRID", "Hybrid"),
                new OptionDto("PHEV", "Plug-in Hybrid")
        );
    }
}
