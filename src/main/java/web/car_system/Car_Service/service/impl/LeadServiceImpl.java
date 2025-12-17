package web.car_system.Car_Service.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.car_system.Car_Service.domain.dto.lead.CreateLeadRequest;
import web.car_system.Car_Service.domain.dto.lead.LeadDto;
import web.car_system.Car_Service.domain.dto.lead.UpdateLeadRequest;
import web.car_system.Car_Service.domain.entity.InventoryCar;
import web.car_system.Car_Service.domain.entity.Lead;
import web.car_system.Car_Service.domain.entity.LeadStatus;
import web.car_system.Car_Service.domain.entity.User;
import web.car_system.Car_Service.domain.mapper.LeadMapper;
import web.car_system.Car_Service.repository.InventoryCarRepository;
import web.car_system.Car_Service.repository.LeadRepository;
import web.car_system.Car_Service.repository.UserRepository;
import web.car_system.Car_Service.service.LeadService;

@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final InventoryCarRepository inventoryCarRepository;
    private final UserRepository userRepository;
    private final LeadMapper leadMapper;

    @Override
    @Transactional
    public LeadDto createLead(CreateLeadRequest request) {
        Lead newLead = leadMapper.toEntity(request);

        // Xử lý gán xe (nếu có)
        if (request.getInventoryCarId() != null) {
            InventoryCar car = inventoryCarRepository.findById(request.getInventoryCarId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy xe với ID: " + request.getInventoryCarId()));
            newLead.setInterestedCar(car);
        }
        newLead.setLeadStatus(LeadStatus.NEW);
        // Lưu và chuyển đổi sang DTO để trả về
        Lead savedLead = leadRepository.save(newLead);
        return leadMapper.toDto(savedLead);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadDto> getAllLeads(Pageable pageable) {
        return leadRepository.findAll(pageable).map(leadMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public LeadDto getLeadById(Long id) {
        return leadRepository.findById(id)
                .map(leadMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lead với ID: " + id));
    }

    @Override
    @Transactional
    public LeadDto updateLead(Long id, UpdateLeadRequest request) {
        Lead existingLead = leadRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lead với ID: " + id));

        // Gán nhân viên phụ trách
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .filter(user -> user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName()))) // Chỉ gán cho Admin
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy nhân viên (Admin) với ID: " + request.getAssigneeId()));
            existingLead.setAssignee(assignee);
        } else {
            existingLead.setAssignee(null);
        }

        // Cập nhật trạng thái
        existingLead.setLeadStatus(request.getLeadStatus());

        Lead updatedLead = leadRepository.save(existingLead);
        return leadMapper.toDto(updatedLead);
    }

    @Override
    @Transactional
    public void deleteLead(Long id) {
        if (!leadRepository.existsById(id)) {
            throw new EntityNotFoundException("Không thể xóa. Không tìm thấy lead với ID: " + id);
        }
        leadRepository.deleteById(id);
    }
}