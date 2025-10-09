package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "regional_fees", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provinceCity"}, name = "uk_regionalfee_province")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionalFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "province_city", nullable = false, length = 100)
    private String provinceCity;

    @Column(name = "registration_fee_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal registrationFeeRate;

    @Column(name = "license_plate_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal licensePlateFee;

    @Column(name = "road_usage_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal roadUsageFee;

    @Column(name = "inspection_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal inspectionFee;

    @Column(name = "civil_liability_insurance_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal civilLiabilityInsuranceFee;
}