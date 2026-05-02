package web.car_system.Car_Service.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import web.car_system.Car_Service.domain.dto.sales_order.CreateOrderRequest;
import web.car_system.Car_Service.domain.dto.sales_order.OrderDto;
import web.car_system.Car_Service.domain.entity.InventoryCar;
import web.car_system.Car_Service.domain.entity.OrderStatus;
import web.car_system.Car_Service.domain.entity.PaymentStatus;
import web.car_system.Car_Service.domain.entity.SaleStatus;
import web.car_system.Car_Service.domain.entity.SalesOrder;
import web.car_system.Car_Service.domain.mapper.OrderStatusHistoryMapper;
import web.car_system.Car_Service.domain.mapper.SalesOrderMapper;
import web.car_system.Car_Service.exception.BusinessException;
import web.car_system.Car_Service.repository.InventoryCarRepository;
import web.car_system.Car_Service.repository.LeadRepository;
import web.car_system.Car_Service.repository.OrderStatusHistoryRepository;
import web.car_system.Car_Service.repository.SalesOrderRepository;
import web.car_system.Car_Service.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SalesOrderServiceImplTest {

    // Khai báo đủ 7 Repository/Mapper mà SalesOrderServiceImpl đang cần
    @Mock private SalesOrderRepository salesOrderRepository;
    @Mock private InventoryCarRepository inventoryCarRepository;
    @Mock private UserRepository userRepository;
    @Mock private LeadRepository leadRepository;
    @Mock private SalesOrderMapper salesOrderMapper;
    @Mock private OrderStatusHistoryRepository statusHistoryRepository;
    @Mock private OrderStatusHistoryMapper statusHistoryMapper;

    @InjectMocks
    private SalesOrderServiceImpl salesOrderService;

    private CreateOrderRequest createRequest;
    private InventoryCar mockCar;
    private SalesOrder mockOrder;
    private OrderDto mockOrderDto;

    @BeforeEach
    void setUp() {
        // 1. Khởi tạo xe trong kho
        mockCar = new InventoryCar();
        mockCar.setId(100L);
        mockCar.setSaleStatus(SaleStatus.AVAILABLE);

        // 2. Dùng lenient() cho các stubbing dùng chung để tránh lỗi bới lông tìm vết của Mockito
        createRequest = mock(CreateOrderRequest.class);
        lenient().when(createRequest.getInventoryCarId()).thenReturn(100L);
        lenient().when(createRequest.getDepositAmount()).thenReturn(new BigDecimal("200000000"));

        // 3. Dùng Mock hoàn toàn cho SalesOrder để tránh lỗi NullPointerException từ bên trong Entity
        mockOrder = mock(SalesOrder.class);
        lenient().when(mockOrder.getId()).thenReturn(1L);
        lenient().when(mockOrder.getOrderStatus()).thenReturn(OrderStatus.DRAFT);
        lenient().when(mockOrder.getTotalPrice()).thenReturn(new BigDecimal("1000000000"));
        lenient().when(mockOrder.getRemainingAmount()).thenReturn(new BigDecimal("1000000000"));
        lenient().when(mockOrder.getInventoryCar()).thenReturn(mockCar);
        lenient().when(mockOrder.getPaymentStatus()).thenReturn(PaymentStatus.FULLY_PAID);

        // --- BỔ SUNG 3 DÒNG NÀY ĐỂ FIX LỖI ---
        lenient().when(createRequest.getCustomerName()).thenReturn("Nguyễn Đình Hội"); // Cho log in ra tên đẹp thay vì null
        lenient().when(createRequest.getSalesStaffId()).thenReturn(null); // Bỏ qua logic tìm nhân viên
        lenient().when(createRequest.getLeadId()).thenReturn(null);       // Bỏ qua logic tìm Lead

        mockOrderDto = mock(OrderDto.class);
    }

    @Test
    @DisplayName("TC1: Tạo đơn hàng THÀNH CÔNG, xe chuyển sang trạng thái RESERVED")
    void createOrder_Success_CarBecomesReserved() {
        // GIVEN
        when(inventoryCarRepository.findById(100L)).thenReturn(Optional.of(mockCar));
        when(salesOrderRepository.findActiveOrderByInventoryCarId(100L)).thenReturn(Optional.empty());
        when(salesOrderMapper.toEntity(createRequest)).thenReturn(mockOrder);
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(mockOrder);
        when(salesOrderMapper.toDto(any(SalesOrder.class))).thenReturn(mockOrderDto);

        // WHEN
        OrderDto result = salesOrderService.createOrder(createRequest);

        // THEN
        assertNotNull(result);
        assertEquals(SaleStatus.RESERVED, mockCar.getSaleStatus(), "Xe phải bị khóa (RESERVED) sau khi lên đơn");

        verify(inventoryCarRepository, times(1)).save(mockCar);
        verify(salesOrderRepository, times(1)).save(mockOrder);
    }

    @Test
    @DisplayName("TC2: Tạo đơn hàng THẤT BẠI vì xe đã có người khác đặt (RESERVED)")
    void createOrder_Fail_WhenCarIsReserved() {
        // GIVEN
        mockCar.setSaleStatus(SaleStatus.RESERVED);
        when(inventoryCarRepository.findById(100L)).thenReturn(Optional.of(mockCar));

        // WHEN & THEN
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            salesOrderService.createOrder(createRequest);
        });

        assertTrue(exception.getMessage().contains("đã được đặt trước"), "Phải báo lỗi xe đã được đặt trước");
        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("TC3: Cập nhật trạng thái THẤT BẠI khi nhảy cóc (DRAFT -> COMPLETED)")
    void updateOrderStatus_Fail_InvalidTransition() {
        // GIVEN
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        // WHEN & THEN
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            salesOrderService.updateOrderStatus(1L, OrderStatus.COMPLETED, "Khách giục giao xe gấp");
        });

        assertTrue(exception.getMessage().contains("Không thể chuyển từ"),
                "State Machine phải chặn lại việc nhảy cóc trạng thái");
        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("TC4: Hủy đơn hàng THÀNH CÔNG, nhả kho xe về lại AVAILABLE")
    void cancelOrder_Success_CarBecomesAvailable() {
        // GIVEN
        mockCar.setSaleStatus(SaleStatus.RESERVED); // Đang bị giữ cọc

        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(mockOrder);
        when(salesOrderMapper.toDto(any(SalesOrder.class))).thenReturn(mockOrderDto);

        // WHEN
        OrderDto result = salesOrderService.cancelOrder(1L, "Khách đổi ý");

        // THEN
        assertNotNull(result);

        // Vì dùng Mock, ta dùng phương thức verify để theo dõi hành vi cập nhật trạng thái của object
        verify(mockOrder).setOrderStatus(OrderStatus.CANCELLED);
        verify(mockOrder).setCancellationReason("Khách đổi ý");

        assertEquals(SaleStatus.AVAILABLE, mockCar.getSaleStatus(), "Kho xe phải được nhả ra (AVAILABLE)");

        verify(inventoryCarRepository, times(1)).save(mockCar);
        verify(statusHistoryRepository, times(1)).save(any());
    }
}