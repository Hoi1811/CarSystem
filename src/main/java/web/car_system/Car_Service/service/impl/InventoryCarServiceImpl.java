package web.car_system.Car_Service.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.dto.inventory_car.CreateInventoryCarRequest;
import web.car_system.Car_Service.domain.dto.inventory_car.InventoryCarDto;
import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.domain.entity.InventoryCar;
import web.car_system.Car_Service.domain.entity.SaleStatus;
import web.car_system.Car_Service.domain.mapper.InventoryCarMapper;
import web.car_system.Car_Service.repository.CarRepository;
import web.car_system.Car_Service.repository.InventoryCarRepository;
import web.car_system.Car_Service.service.InventoryCarService;
@Service
@RequiredArgsConstructor
public class InventoryCarServiceImpl implements InventoryCarService {
    private final InventoryCarRepository inventoryCarRepository;
    private final CarRepository carRepository;

    private final InventoryCarMapper inventoryCarMapper; // Tiêm Mapper vào

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
    @Transactional(readOnly = true) // readOnly = true để tối ưu hóa hiệu năng cho các truy vấn chỉ đọc
    public Page<InventoryCarDto> getAllAvailableCars(Pageable pageable) {
        // Gọi phương thức repository đã tạo để lấy danh sách xe đang bán, có phân trang
        Page<InventoryCar> availableCarsPage = inventoryCarRepository.findAllBySaleStatus(SaleStatus.AVAILABLE, pageable);

        // Dùng stream của Java và mapper để chuyển đổi Page<Entity> thành Page<DTO>
        // Đây là cách làm chuẩn khi làm việc với Page trong Spring
        return availableCarsPage.map(inventoryCarMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryCarDto> getAllInventoryCarsForAdmin(Pageable pageable) {
        // Dùng phương thức findAll mặc định của JpaRepository
        Page<InventoryCar> allCarsPage = inventoryCarRepository.findAll(pageable);

        // Tương tự, dùng map để chuyển đổi sang DTO
        return allCarsPage.map(inventoryCarMapper::toDto);
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
}
