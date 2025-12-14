package web.car_system.Car_Service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import web.car_system.Car_Service.domain.dto.lead.CreateLeadRequest;
import web.car_system.Car_Service.domain.dto.lead.LeadDto;
import web.car_system.Car_Service.domain.dto.lead.UpdateLeadRequest;

public interface LeadService {
    LeadDto createLead(CreateLeadRequest request);

    Page<LeadDto> getAllLeads(Pageable pageable);

    LeadDto getLeadById(Long id);

    LeadDto updateLead(Long id, UpdateLeadRequest request);

    void deleteLead(Long id);
}
