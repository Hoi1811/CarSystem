package web.car_system.Car_Service.domain.entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ControlType {
    TEXT_INPUT("Text Input (Chữ)"),
    NUMBER_INPUT("Number Input (Số)"),
    SINGLE_SELECT("Single Select (Dropdown)"),
    BOOLEAN_SELECT("Boolean (Có/Không)"),
    POWER_TORQUE_INPUT("Power/Torque (Công suất/Mô-men xoắn)"),
    DIMENSION_INPUT("Dimension (Kích thước Dài/Rộng/Cao)");

    private final String displayName;
}