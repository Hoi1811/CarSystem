package web.car_system.Car_Service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.car_system.Car_Service.annotation.RestApiV1;
import web.car_system.Car_Service.domain.dto.global.GlobalResponseDTO;
import web.car_system.Car_Service.domain.dto.global.NoPaginatedMeta;
import web.car_system.Car_Service.domain.dto.global.PaginatedMeta;
import web.car_system.Car_Service.domain.dto.lead.CreateLeadRequest;
import web.car_system.Car_Service.domain.dto.lead.LeadDto;
import web.car_system.Car_Service.domain.dto.lead.LeadFilterRequest;
import web.car_system.Car_Service.domain.dto.lead.UpdateLeadRequest;
import web.car_system.Car_Service.service.LeadService;

import java.util.List;

import static web.car_system.Car_Service.constant.Endpoint.V1.LEAD.*;
import static web.car_system.Car_Service.utility.ResponseFactory.success;
import static web.car_system.Car_Service.utility.ResponseFactory.successPageable;

@RestApiV1
@RequiredArgsConstructor
public class LeadController {
    private final LeadService leadService;

    // === PUBLIC ENDPOINT ===

    @PostMapping(SUBMIT_LEAD)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, LeadDto>> submitLead(
            @Valid @RequestBody CreateLeadRequest request) {
        LeadDto createdLead = leadService.createLead(request);
        return success(createdLead, "Gửi yêu cầu thành công! Chúng tôi sẽ liên hệ với bạn sớm.", HttpStatus.CREATED);
    }

    // === ADMIN ENDPOINTS ===

    @GetMapping(GET_ALL)
    public ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<LeadDto>>> getAllLeads(Pageable pageable) {
        Page<LeadDto> leadPage = leadService.getAllLeads(pageable);
        return successPageable(leadPage, "Lấy danh sách khách hàng tiềm năng thành công.");
    }
    
    /**
     * Search/filter leads với các tiêu chí phức tạp
     * Sử dụng GET với query parameters
     */
    @GetMapping(SEARCH)
    public ResponseEntity<GlobalResponseDTO<PaginatedMeta, List<LeadDto>>> searchLeads(
            @ModelAttribute LeadFilterRequest filter,
            Pageable pageable) {
        Page<LeadDto> leadPage = leadService.searchLeads(filter, pageable);
        return successPageable(leadPage, "Tìm kiếm khách hàng tiềm năng thành công.");
    }

    @GetMapping(GET_BY_ID)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, LeadDto>> getLeadById(@PathVariable Long id) {
        LeadDto lead = leadService.getLeadById(id);
        return success(lead, "Lấy chi tiết lead thành công.");
    }

    @PutMapping(UPDATE)
    public ResponseEntity<GlobalResponseDTO<NoPaginatedMeta, LeadDto>> updateLead(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLeadRequest request) {
        LeadDto updatedLead = leadService.updateLead(id, request);
        return success(updatedLead, "Cập nhật lead thành công.");
    }

    @DeleteMapping(DELETE)
    public ResponseEntity<Void> deleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
        return ResponseEntity.noContent().build();
    }
}
