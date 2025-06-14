package web.car_system.Car_Service.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Origin {
    IMPORTED("Nhập khẩu"),
    LOCALLY_ASSEMBLED("Lắp ráp"),
    DOMESTIC_MANUFACTURING("Sản xuất trong nước");

    private final String displayName;

}