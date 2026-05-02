package web.car_system.Car_Service.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;
import web.car_system.Car_Service.domain.dto.car.CarDetailsResponseDTO;
import web.car_system.Car_Service.domain.dto.car.CarResponseDTO;
import web.car_system.Car_Service.domain.dto.global.FilterCarPaginationRequestDTO;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.domain.mapper.CarMapper;
import web.car_system.Car_Service.repository.CarRepository;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarServiceImplTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper; // Service có @RequiredArgsConstructor nên vẫn cần mock để pass @InjectMocks

    @InjectMocks
    private CarServiceImpl carService;

    private Car mockCar;

    @BeforeEach
    void setUp() {
        // Khởi tạo dữ liệu giả cho Car entity (ID dùng Integer, không phải Long)
        mockCar = new Car();
        mockCar.setCarId(1);
        mockCar.setName("Toyota Camry");
        mockCar.setModel("2024");
    }

    // =========================================================================
    // USE CASE: XEM THÔNG TIN CHI TIẾT XE Ô TÔ
    // =========================================================================

    @Test
    @DisplayName("Lấy chi tiết xe THÀNH CÔNG khi truyền ID hợp lệ")
    void getCarById_Success_WhenIdIsValid() {
        // GIVEN
        when(carRepository.findById(1)).thenReturn(Optional.of(mockCar));

        // WHEN (Gọi trực tiếp service. Vì MapStruct sinh CarMapper.INSTANCE thực, nó sẽ tự động map đúng)
        GlobalResponseDTO<?, CarDetailsResponseDTO> response = carService.getCarById(1);

        // THEN
        assertNotNull(response);
        assertNotNull(response.data()); // Sửa getData() thành data()
        assertEquals(1, response.data().carId());
        assertEquals("Toyota Camry", response.data().name());

        verify(carRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Lấy chi tiết xe THẤT BẠI, trả về Response lỗi khi ID không tồn tại")
    void getCarById_Fail_WhenIdDoesNotExist() {
        // GIVEN
        when(carRepository.findById(999)).thenReturn(Optional.empty());

        // WHEN
        GlobalResponseDTO<?, CarDetailsResponseDTO> response = carService.getCarById(999);

        // THEN
        assertNotNull(response);
        assertNull(response.data()); // Vì nhảy vào catch nên data sẽ là null

        // Kiểm tra đối tượng meta xem có trả về đúng trạng thái ERROR không
        web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta meta =
                (web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta) response.meta();

        assertEquals(web.car_system.Car_Service.domain.dto.global.Status.ERROR, meta.status());
        assertTrue(meta.message().contains("Lỗi khi lấy thông tin xe"));

        verify(carRepository, times(1)).findById(999);
    }

    // =========================================================================
    // USE CASE: TÌM KIẾM VÀ LỌC DANH SÁCH XE Ô TÔ
    // =========================================================================

    @Test
    @DisplayName("Lọc danh sách xe THÀNH CÔNG trả về kết quả phân trang")
    void filterCars_Success_ReturnsPaginatedResult() {
        // GIVEN
        // Khởi tạo DTO dạng Record (Truyền đúng 12 tham số, truyền null cho các params không dùng đến)
        FilterCarPaginationRequestDTO requestDTO = new FilterCarPaginationRequestDTO(
                null, null, null, null, null,
                0, "ASC", (short) 10, "carId", null, null, null
        );

        Pageable pageable = PageRequest.of(0, 10);
        Page<Car> mockPage = new PageImpl<>(Collections.singletonList(mockCar), pageable, 1);

        // Mock Repository tìm kiếm theo Specification
        when(carRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);

        // WHEN
        // Gọi hàm phân trang lọc dữ liệu thực tế
        var response = carService.getAllCarsPaginated(requestDTO);

        // THEN
        // THEN
        assertNotNull(response);
        assertNotNull(response.data()); // Sửa getData() thành data()
        assertEquals(1, response.data().size());

        verify(carRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }
}