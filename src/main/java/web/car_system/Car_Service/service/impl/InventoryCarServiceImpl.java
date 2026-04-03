package web.car_system.Car_Service.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import web.car_system.Car_Service.domain.dto.inventory_car.CreateInventoryCarRequest;
import web.car_system.Car_Service.domain.dto.inventory_car.InventoryCarDto;
import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.domain.entity.InventoryCar;
import web.car_system.Car_Service.domain.entity.SaleStatus;
import web.car_system.Car_Service.domain.entity.Showroom;
import web.car_system.Car_Service.domain.entity.User;
import web.car_system.Car_Service.domain.mapper.InventoryCarMapper;
import web.car_system.Car_Service.repository.CarRepository;
import web.car_system.Car_Service.repository.InventoryCarRepository;
import web.car_system.Car_Service.repositories.ShowroomRepository;
import web.car_system.Car_Service.service.InventoryCarService;
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryCarServiceImpl implements InventoryCarService {
    private final InventoryCarRepository inventoryCarRepository;
    private final CarRepository carRepository;
    private final ShowroomRepository showroomRepository;
    private final InventoryCarMapper inventoryCarMapper;

    @Override
    @Transactional
    public InventoryCarDto addCarToInventory(CreateInventoryCarRequest request) {
        Car carTemplate = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy mẫu xe với ID: " + request.getCarId()));

        if (request.getVin() != null && inventoryCarRepository.findByVin(request.getVin()).isPresent()) {
            throw new IllegalArgumentException("Số VIN đã tồn tại: " + request.getVin());
        }

        // Bước 1: Dùng Mapper để chuyển request DTO thành Entity
        InventoryCar newInventoryCar = inventoryCarMapper.toEntity(request);

        // Bước 2: Gán các đối tượng quan hệ thủ công
        newInventoryCar.setCar(carTemplate);

        // Tự động gán showroom từ context người dùng đang đăng nhập (Multi-tenant)
        newInventoryCar.setShowroom(resolveActiveShowroom());

        // Bước 3: Xử lý các giá trị mặc định (nếu có)
        if (newInventoryCar.getSaleStatus() == null) {
            newInventoryCar.setSaleStatus(SaleStatus.AVAILABLE);
        }

        // Bước 4: Lưu vào DB
        InventoryCar savedCar = inventoryCarRepository.save(newInventoryCar);

        // Bước 5: Dùng Mapper để chuyển Entity thành DTO trả về
        return inventoryCarMapper.toDto(savedCar);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryCarDto> getAllAvailableCars(Integer carId, Long showroomId, Pageable pageable) {
        Page<InventoryCar> carPage;

        if (carId != null && showroomId != null) {
            carPage = inventoryCarRepository.findAllByCar_CarIdAndSaleStatusAndShowroomId(carId, SaleStatus.AVAILABLE, showroomId, pageable);
        } else if (carId != null) {
            carPage = inventoryCarRepository.findAllByCar_CarIdAndSaleStatus(carId, SaleStatus.AVAILABLE, pageable);
        } else if (showroomId != null) {
            carPage = inventoryCarRepository.findAllBySaleStatusAndShowroomId(SaleStatus.AVAILABLE, showroomId, pageable);
        } else {
            carPage = inventoryCarRepository.findAllBySaleStatus(SaleStatus.AVAILABLE, pageable);
        }

        return carPage.map(inventoryCarMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryCarDto> getAllInventoryCarsForAdmin(Pageable pageable) {
        // Hibernate Filter (TenantFilterAspect) tự động gắn WHERE showroom_id = :tenantId
        // SYSTEM_ADMIN không có filter → thấy toàn bộ
        // Staff/Manager → chỉ thấy xe chi nhánh mình
        return inventoryCarRepository.findAll(pageable).map(inventoryCarMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryCarDto getInventoryCarDetails(Long id) {
        // Bước 1: Tìm kiếm xe trong kho bằng ID
        InventoryCar inventoryCar = inventoryCarRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy xe trong kho với ID: " + id));

        // Bước 2: Dùng mapper để chuyển đổi và trả về
        return inventoryCarMapper.toDto(inventoryCar);
    }
    @Override
    @Transactional
    public InventoryCarDto updateInventoryCar(Long id, CreateInventoryCarRequest request) {
        // Bước 1: Tìm chiếc xe hiện có trong database
        InventoryCar existingCar = inventoryCarRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không thể cập nhật. Không tìm thấy xe với ID: " + id));

        // Bước 2: Kiểm tra xem "mẫu xe" (Car template) có bị thay đổi không.
        // Nếu có, ta phải tìm và cập nhật lại mối quan hệ.
        if (!existingCar.getCar().getCarId().equals(request.getCarId())) {
            Car newCarTemplate = carRepository.findById(request.getCarId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy mẫu xe mới với ID: " + request.getCarId()));
            existingCar.setCar(newCarTemplate);
        }

        // Bước 3: Kiểm tra sự thay đổi của số VIN để đảm bảo tính duy nhất
        if (request.getVin() != null && !request.getVin().equals(existingCar.getVin())) {
            inventoryCarRepository.findByVin(request.getVin()).ifPresent(car -> {
                throw new IllegalArgumentException("Số VIN '" + request.getVin() + "' đã được sử dụng bởi xe khác.");
            });
            existingCar.setVin(request.getVin());
        }

        // Bước 4: Cập nhật các trường thông tin khác từ request
        existingCar.setPrice(request.getPrice());
        existingCar.setColor(request.getColor());
        existingCar.setConditionType(request.getConditionType());
        existingCar.setSaleStatus(request.getSaleStatus());
        existingCar.setMileage(request.getMileage());
        existingCar.setYearOfManufacture(request.getYearOfManufacture());
        existingCar.setNotes(request.getNotes());

        // Giữ nguyên showroom hiện tại — không cho client thay đổi
        // Showroom được quyết định bởi TenantFilterAspect, không phải từ request

        // Bước 5: Spring Data JPA sẽ tự động lưu lại các thay đổi vào DB
        // khi transaction kết thúc, nên không cần gọi save() một cách tường minh.
        // Tuy nhiên, gọi save() vẫn là một cách làm rõ ràng và an toàn.
        InventoryCar updatedCar = inventoryCarRepository.save(existingCar);

        // Bước 6: Dùng mapper để trả về kết quả
        return inventoryCarMapper.toDto(updatedCar);
    }

    @Override
    @Transactional
    public InventoryCarDto updateSaleStatus(Long id, SaleStatus newStatus) {
        // Bước 1: Tìm xe
        InventoryCar carToUpdate = inventoryCarRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không thể cập nhật trạng thái. Không tìm thấy xe với ID: " + id));

        // (Tùy chọn) Thêm logic nghiệp vụ phức tạp hơn ở đây nếu cần.
        // Ví dụ: kiểm tra xem có được phép chuyển từ SOLD về AVAILABLE không.
        // if (carToUpdate.getSaleStatus() == SaleStatus.SOLD && newStatus == SaleStatus.AVAILABLE) {
        //     throw new IllegalStateException("Không thể chuyển trạng thái của xe đã bán về 'Đang bán'.");
        // }

        // Bước 2: Cập nhật trạng thái
        carToUpdate.setSaleStatus(newStatus);

        // Bước 3: Lưu lại và trả về kết quả
        InventoryCar updatedCar = inventoryCarRepository.save(carToUpdate);

        return inventoryCarMapper.toDto(updatedCar);
    }

    @Override
    @Transactional
    public void deleteInventoryCar(Long id) {
        // Bước 1: Kiểm tra sự tồn tại của xe trước khi xóa.
        // Nếu không tìm thấy, findById đã văng exception rồi, nhưng existsById là
        // một cách kiểm tra nhẹ nhàng hơn mà không cần load cả entity.
        if (!inventoryCarRepository.existsById(id)) {
            throw new EntityNotFoundException("Không thể xóa. Không tìm thấy xe với ID: " + id);
        }

        // (Tùy chọn) Thêm logic nghiệp vụ trước khi xóa.
        // Ví dụ: Không cho phép xóa xe đã có người đặt cọc hoặc đã bán.
        // InventoryCar car = inventoryCarRepository.findById(id).get(); // Chỉ gọi nếu thực sự cần check
        // if (car.getSaleStatus() == SaleStatus.RESERVED || car.getSaleStatus() == SaleStatus.SOLD) {
        //     throw new IllegalStateException("Không thể xóa xe đã có giao dịch (đặt cọc hoặc đã bán).");
        // }

        // Bước 2: Gọi phương thức deleteById.
        // Spring Data JPA và Hibernate sẽ tự động xử lý để thực hiện câu lệnh UPDATE
        // thay vì DELETE nhờ vào annotation @SQLDelete trong Entity.
        inventoryCarRepository.deleteById(id);
    }

    // ===== HELPER: Xác định Showroom dựa trên SecurityContext =====

    /**
     * Xác định showroom hiện tại dựa trên user đang đăng nhập.
     * - Staff/Manager: trả về showroom đã gán trên tài khoản.
     * - System Admin: đọc header X-Tenant-ID để biết đang thao tác chi nhánh nào.
     * - Nếu không xác định được: trả về null (xe không gán chi nhánh).
     */
    private Showroom resolveActiveShowroom() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !(auth.getPrincipal() instanceof User)) return null;

            User currentUser = (User) auth.getPrincipal();

            // Staff/Manager: luôn gán theo showroom của họ
            if (currentUser.getShowroom() != null) {
                return currentUser.getShowroom();
            }

            // System Admin: đọc X-Tenant-ID header
            boolean isAdmin = currentUser.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("ROLE_SYSTEM_ADMIN") || role.getName().equals("SYSTEM_ADMIN"));
            if (isAdmin) {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    String tenantHeader = attrs.getRequest().getHeader("X-Tenant-ID");
                    if (tenantHeader != null && !tenantHeader.isEmpty()) {
                        return showroomRepository.findById(Long.parseLong(tenantHeader))
                                .orElse(null);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Không thể xác định showroom cho xe mới: {}", e.getMessage());
        }
        return null;
    }
}
