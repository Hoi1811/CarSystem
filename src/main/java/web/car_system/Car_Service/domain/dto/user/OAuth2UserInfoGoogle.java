package web.car_system.Car_Service.domain.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public record OAuth2UserInfoGoogle(
        @JsonProperty("sub")
        String id,
        String name,
        String email,
        String picture
) implements OAuth2UserInfo {
}
