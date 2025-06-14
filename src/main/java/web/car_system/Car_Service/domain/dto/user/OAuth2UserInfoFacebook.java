package web.car_system.Car_Service.domain.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OAuth2UserInfoFacebook(
        String id,
        String name,
        String email,
        @JsonProperty("picture.data.url")
        String picture
) implements OAuth2UserInfo {
}
