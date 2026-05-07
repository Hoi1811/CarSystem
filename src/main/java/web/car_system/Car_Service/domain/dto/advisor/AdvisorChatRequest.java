package web.car_system.Car_Service.domain.dto.advisor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdvisorChatRequest(
        @NotBlank(message = "query không được rỗng")
        @Size(max = 2000, message = "query tối đa 2000 ký tự")
        String query,

        String sessionId
) {}
