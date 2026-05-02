package web.car_system.Car_Service.service.impl;

import jakarta.persistence.EntityNotFoundException;
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
import web.car_system.Car_Service.domain.dto.inventory_car.InventoryCarDto;
import web.car_system.Car_Service.domain.entity.InventoryCar;
import web.car_system.Car_Service.domain.entity.SaleStatus;
import web.car_system.Car_Service.domain.mapper.InventoryCarMapper;
import web.car_system.Car_Service.repository.InventoryCarRepository;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryCarServiceImplTest {

    @Mock
    private InventoryCarRepository inventoryCarRepository;

    @Mock
    private InventoryCarMapper inventoryCarMapper;

    @InjectMocks
    private InventoryCarServiceImpl inventoryCarService;

    private InventoryCar mockInventoryCar;
    private InventoryCarDto mockInventoryCarDto;

    @BeforeEach
    void setUp() {
        // Khởi tạo dữ liệu giả lập (Mock Data)
        mockInventoryCar = new InventoryCar();
        mockInventoryCar.setId(1L);
        mockInventoryCar.setVin("VIN1234567890");
        mockInventoryCar.setSaleStatus(SaleStatus.AVAILABLE);

        // Tạo giả một DTO để mapper trả về
        mockInventoryCarDto = mock(InventoryCarDto.class);
    }

    // =========================================================================
    // 1. KIỂM THỬ XEM CHI TIẾT XE TRONG KHO (DETAIL)
    // =========================================================================

    @Test
    @DisplayName("Lấy chi tiết xe trong kho THÀNH CÔNG khi truyền đúng ID")
    void getInventoryCarDetails_Success() {
        // GIVEN
        when(inventoryCarRepository.findById(1L)).thenReturn(Optional.of(mockInventoryCar));
        when(inventoryCarMapper.toDto(mockInventoryCar)).thenReturn(mockInventoryCarDto);

        // WHEN
        InventoryCarDto result = inventoryCarService.getInventoryCarDetails(1L);

        // THEN
        assertNotNull(result, "Kết quả trả về không được NULL");
        verify(inventoryCarRepository, times(1)).findById(1L);
        verify(inventoryCarMapper, times(1)).toDto(mockInventoryCar);
    }

    @Test
    @DisplayName("Lấy chi tiết xe THẤT BẠI, ném EntityNotFoundException khi ID ảo")
    void getInventoryCarDetails_Fail_WhenIdNotFound() {
        // GIVEN
        when(inventoryCarRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN & THEN
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            inventoryCarService.getInventoryCarDetails(999L);
        });

        // Đảm bảo thông báo lỗi chứa từ khóa chính xác như code bạn viết
        assertTrue(exception.getMessage().contains("Không tìm thấy xe trong kho"));

        verify(inventoryCarRepository, times(1)).findById(999L);
        // Chắc chắn mapper không bị gọi khi DB không có dữ liệu
        verify(inventoryCarMapper, never()).toDto(any());
    }

    // =========================================================================
    // 2. KIỂM THỬ TÌM KIẾM/LỌC DANH SÁCH XE TẠI ĐẠI LÝ
    // =========================================================================

    @Test
    @DisplayName("Lấy danh sách xe rảnh (AVAILABLE) THÀNH CÔNG khi LỌC theo cả CarID và ShowroomID")
    void getAllAvailableCars_Success_WithBothFilters() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        Page<InventoryCar> mockPage = new PageImpl<>(Collections.singletonList(mockInventoryCar));

        when(inventoryCarRepository.findAllByCar_CarIdAndSaleStatusAndShowroomId(
                eq(100), eq(SaleStatus.AVAILABLE), eq(2L), eq(pageable)
        )).thenReturn(mockPage);

        when(inventoryCarMapper.toDto(any(InventoryCar.class))).thenReturn(mockInventoryCarDto);

        // WHEN
        Page<InventoryCarDto> result = inventoryCarService.getAllAvailableCars(100, 2L, pageable);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        // Đảm bảo hệ thống gọi đúng hàm truy vấn DB tương ứng
        verify(inventoryCarRepository, times(1))
                .findAllByCar_CarIdAndSaleStatusAndShowroomId(100, SaleStatus.AVAILABLE, 2L, pageable);
    }

    @Test
    @DisplayName("Lấy danh sách xe rảnh THÀNH CÔNG khi KHÔNG truyền điều kiện lọc (Lấy tất cả)")
    void getAllAvailableCars_Success_WithNoFilters() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        Page<InventoryCar> mockPage = new PageImpl<>(Collections.singletonList(mockInventoryCar));

        when(inventoryCarRepository.findAllBySaleStatus(eq(SaleStatus.AVAILABLE), eq(pageable)))
                .thenReturn(mockPage);

        when(inventoryCarMapper.toDto(any(InventoryCar.class))).thenReturn(mockInventoryCarDto);

        // WHEN (Truyền null cho cả 2 bộ lọc)
        Page<InventoryCarDto> result = inventoryCarService.getAllAvailableCars(null, null, pageable);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(inventoryCarRepository, times(1)).findAllBySaleStatus(SaleStatus.AVAILABLE, pageable);
    }

    @Test
    @DisplayName("Admin lấy toàn bộ danh sách kho xe THÀNH CÔNG")
    void getAllInventoryCarsForAdmin_Success() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        Page<InventoryCar> mockPage = new PageImpl<>(Collections.singletonList(mockInventoryCar));

        when(inventoryCarRepository.findAll(pageable)).thenReturn(mockPage);
        when(inventoryCarMapper.toDto(any(InventoryCar.class))).thenReturn(mockInventoryCarDto);

        // WHEN
        Page<InventoryCarDto> result = inventoryCarService.getAllInventoryCarsForAdmin(pageable);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(inventoryCarRepository, times(1)).findAll(pageable);
    }
}