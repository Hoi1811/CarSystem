package web.car_system.Car_Service.domain.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserCreateRequestDTO(
        @JsonProperty("user_id")
        Long userId,
        String email,
        @JsonProperty("external_id")
        String externalId,
        @JsonProperty("full_name")
        String fullName,

        String provider,

        String picture
) {
}
