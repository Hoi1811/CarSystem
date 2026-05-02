package web.car_system.Car_Service.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import web.car_system.Car_Service.domain.dto.lead.CreateLeadRequest;
import web.car_system.Car_Service.domain.dto.lead.LeadDto;
import web.car_system.Car_Service.domain.dto.lead.UpdateLeadRequest;
import web.car_system.Car_Service.domain.entity.InventoryCar;
import web.car_system.Car_Service.domain.entity.Lead;
import web.car_system.Car_Service.domain.entity.LeadStatus;
import web.car_system.Car_Service.domain.entity.Role;
import web.car_system.Car_Service.domain.entity.User;
import web.car_system.Car_Service.domain.mapper.LeadMapper;
import web.car_system.Car_Service.repository.InventoryCarRepository;
import web.car_system.Car_Service.repository.LeadRepository;
import web.car_system.Car_Service.repository.UserRepository;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeadServiceImplTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private InventoryCarRepository inventoryCarRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LeadMapper leadMapper;

    @InjectMocks
    private LeadServiceImpl leadService;

    private Lead mockLead;
    private LeadDto mockLeadDto;
    private CreateLeadRequest createRequest;

    @BeforeEach
    void setUp() {
        // Dữ liệu giả định dùng chung
        mockLead = new Lead();
        mockLead.setId(1L);

        mockLeadDto = mock(LeadDto.class);
        createRequest = mock(CreateLeadRequest.class);
    }

    // =========================================================================
    // TC 1-3: TẠO HỒ SƠ TƯ VẤN (CREATE LEAD)
    // =========================================================================

    @Test
    @DisplayName("TC1: Tạo Lead THÀNH CÔNG khi khách hàng CÓ CHỌN xe quan tâm")
    void createLead_Success_WithInterestedCar() {
        // GIVEN
        when(createRequest.getInventoryCarId()).thenReturn(100L);
        when(leadMapper.toEntity(createRequest)).thenReturn(mockLead);

        InventoryCar mockCar = new InventoryCar();
        mockCar.setId(100L);
        when(inventoryCarRepository.findById(100L)).thenReturn(Optional.of(mockCar));

        when(leadRepository.save(any(Lead.class))).thenReturn(mockLead);
        when(leadMapper.toDto(any(Lead.class))).thenReturn(mockLeadDto);

        // WHEN
        LeadDto result = leadService.createLead(createRequest);

        // THEN
        assertNotNull(result);
        assertEquals(LeadStatus.NEW, mockLead.getLeadStatus(), "Trạng thái Lead mới phải là NEW");
        assertNotNull(mockLead.getInterestedCar(), "Phải gán được xe khách hàng quan tâm");

        verify(inventoryCarRepository, times(1)).findById(100L);
        verify(leadRepository, times(1)).save(mockLead);
    }

    @Test
    @DisplayName("TC2: Tạo Lead THÀNH CÔNG khi khách hàng KHÔNG CHỌN cụ thể xe nào")
    void createLead_Success_WithoutInterestedCar() {
        // GIVEN
        when(createRequest.getInventoryCarId()).thenReturn(null); // Không truyền ID xe
        when(leadMapper.toEntity(createRequest)).thenReturn(mockLead);

        when(leadRepository.save(any(Lead.class))).thenReturn(mockLead);
        when(leadMapper.toDto(any(Lead.class))).thenReturn(mockLeadDto);

        // WHEN
        LeadDto result = leadService.createLead(createRequest);

        // THEN
        assertNotNull(result);
        assertEquals(LeadStatus.NEW, mockLead.getLeadStatus());

        // Không bao giờ được gọi xuống bảng InventoryCar
        verify(inventoryCarRepository, never()).findById(any());
        verify(leadRepository, times(1)).save(mockLead);
    }

    @Test
    @DisplayName("TC3: Tạo Lead THẤT BẠI khi truyền ID xe tào lao")
    void createLead_Fail_WhenCarNotFound() {
        // GIVEN
        when(createRequest.getInventoryCarId()).thenReturn(999L);
        when(leadMapper.toEntity(createRequest)).thenReturn(mockLead);
        when(inventoryCarRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN & THEN
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            leadService.createLead(createRequest);
        });

        assertTrue(exception.getMessage().contains("Không tìm thấy xe"));
        verify(leadRepository, never()).save(any(Lead.class));
    }

    // =========================================================================
    // TC 4-5: CẬP NHẬT TRẠNG THÁI & GÁN NHÂN VIÊN XỬ LÝ
    // =========================================================================

    @Test
    @DisplayName("TC4: Cập nhật Lead THÀNH CÔNG, gán cho nhân viên có quyền ROLE_ADMIN")
    void updateLead_Success_AssignToAdmin() {
        // GIVEN
        UpdateLeadRequest updateReq = mock(UpdateLeadRequest.class);
        when(updateReq.getAssigneeId()).thenReturn(5L);
        when(updateReq.getLeadStatus()).thenReturn(LeadStatus.NEW); // Sử dụng trạng thái chắc chắn tồn tại

        when(leadRepository.findById(1L)).thenReturn(Optional.of(mockLead));

        // Giả lập User có quyền ROLE_ADMIN
        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        User adminUser = new User();
        adminUser.setUserId(5L); // Đã sửa thành setUserId
        adminUser.setRoles(Set.of(adminRole)); // Đã sửa thành Set.of()

        when(userRepository.findById(5L)).thenReturn(Optional.of(adminUser));
        when(leadRepository.save(any(Lead.class))).thenReturn(mockLead);
        when(leadMapper.toDto(any(Lead.class))).thenReturn(mockLeadDto);

        // WHEN
        LeadDto result = leadService.updateLead(1L, updateReq);

        // THEN
        assertNotNull(result);
        assertEquals(adminUser, mockLead.getAssignee(), "Phải gán đúng nhân viên Admin");
        assertEquals(LeadStatus.NEW, mockLead.getLeadStatus(), "Phải cập nhật đúng trạng thái");
        verify(leadRepository, times(1)).save(mockLead);
    }

    @Test
    @DisplayName("TC5: Cập nhật Lead THẤT BẠI vì nhân viên được gán KHÔNG CÓ quyền ROLE_ADMIN")
    void updateLead_Fail_AssigneeNotAdmin() {
        // GIVEN
        UpdateLeadRequest updateReq = mock(UpdateLeadRequest.class);
        when(updateReq.getAssigneeId()).thenReturn(10L);

        when(leadRepository.findById(1L)).thenReturn(Optional.of(mockLead));

        // Giả lập User chỉ có quyền USER (Không có ROLE_ADMIN)
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        User normalUser = new User();
        normalUser.setUserId(10L); // Đã sửa thành setUserId
        normalUser.setRoles(Set.of(userRole)); // Đã sửa thành Set.of()

        when(userRepository.findById(10L)).thenReturn(Optional.of(normalUser));

        // WHEN & THEN
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            leadService.updateLead(1L, updateReq);
        });

        assertTrue(exception.getMessage().contains("Không tìm thấy nhân viên (Admin)"),
                "Phải chặn việc gán Lead cho người không phải Admin");
        verify(leadRepository, never()).save(any(Lead.class));
    }
}