package web.car_system.Car_Service.domain.entity;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Map giữa code (URL slug, dễ đọc) và Class entity được audit qua Hibernate Envers.
 * Dùng cho generic AuditController/AuditService — tránh phải copy code cho từng entity.
 *
 * <p>Sau Batch 5 (phương án B): 9 entity được audit. {@code Image} và {@code CarAttribute}
 * đã được bỏ {@code @Audited} ngày 2026-05-02.</p>
 */
public enum AuditedEntityType {

    CAR("car", Car.class, "Xe"),
    SPECIFICATION("specification", Specification.class, "Nhóm thông số"),
    ATTRIBUTE("attribute", Attribute.class, "Thuộc tính"),
    COMPARISON_RULE("comparison-rule", ComparisonRule.class, "Quy tắc so sánh"),
    MANUFACTURER("manufacturer", Manufacturer.class, "Hãng sản xuất"),
    CAR_TYPE("car-type", CarType.class, "Loại xe"),
    CAR_SEGMENT("car-segment", CarSegment.class, "Phân khúc"),
    CAR_SEGMENT_GROUP("car-segment-group", CarSegmentGroup.class, "Nhóm phân khúc"),
    CAR_ISSUE_REPORT("car-issue-report", CarIssueReport.class, "Báo lỗi xe");

    private final String code;
    private final Class<?> entityClass;
    private final String displayName;

    AuditedEntityType(String code, Class<?> entityClass, String displayName) {
        this.code = code;
        this.entityClass = entityClass;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    private static final Map<String, AuditedEntityType> BY_CODE =
            Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(AuditedEntityType::getCode, e -> e));

    private static final Map<Class<?>, AuditedEntityType> BY_CLASS =
            Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(AuditedEntityType::getEntityClass, e -> e));

    public static AuditedEntityType resolve(String code) {
        AuditedEntityType type = BY_CODE.get(code);
        if (type == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Không hỗ trợ audit cho entityType: " + code);
        }
        return type;
    }

    public static AuditedEntityType fromClass(Class<?> entityClass) {
        return BY_CLASS.get(entityClass);
    }
}
