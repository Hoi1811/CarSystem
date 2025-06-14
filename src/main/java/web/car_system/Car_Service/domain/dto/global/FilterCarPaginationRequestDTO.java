package web.car_system.Car_Service.domain.dto.global;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import web.car_system.Car_Service.constant.PaginationConstant;
import web.car_system.Car_Service.domain.entity.Origin;
import web.car_system.Car_Service.validation.PriceRangeValid; // Giữ lại nếu bạn dùng

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream; // Thêm import này

@PriceRangeValid
public record FilterCarPaginationRequestDTO(
        @JsonProperty("manufacturer_ids")
        @Pattern(regexp = "^(\\d+(,\\s*\\d+)*)?$", message = "Manufacturer IDs must be a comma-separated list of numbers, or empty.")
        String manufacturerIds,

        @JsonProperty("car_type_ids")
        @Pattern(regexp = "^(\\d+(,\\s*\\d+)*)?$", message = "Car Type IDs must be a comma-separated list of numbers, or empty.")
        String carTypeIds,

        @JsonProperty("car_segment_ids")
        @Pattern(regexp = "^(\\d+(,\\s*\\d+)*)?$", message = "Car Segment IDs must be a comma-separated list of numbers, or empty.")
        String carSegmentIds,

        @JsonProperty("origin")
        // Không cần annotation validate phức tạp ở đây nữa, sẽ dùng @AssertTrue
        String origin,

        @JsonProperty("keyword")
        @Size(max = 255, message = "Keyword cannot exceed 255 characters.")
        String keyword,

        @JsonProperty("index")
        @Min(value = 0, message = "Page index must be a non-negative number.")
        Integer index,

        @JsonProperty("direction")
        @Pattern(regexp = "ASC|DESC", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Direction must be 'ASC' or 'DESC'.")
        String direction,

        @JsonProperty("limit")
        @Min(value = 1, message = "Page size must be at least 1.")
        @Max(value = 100, message = "Page size cannot exceed 100.")
        Short limit,

        @JsonProperty("fields")
        @Pattern(regexp = "^([a-zA-Z0-9_]+(,\\s*[a-zA-Z0-9_]+)*)?$", message = "Fields must be a comma-separated list of valid field names (alphanumeric and underscore), or empty.")
        String fields,

        @JsonProperty("min_price")
        @DecimalMin(value = "0.0", inclusive = true, message = "Minimum price must be non-negative.")
        BigDecimal minPrice,

        @JsonProperty("max_price")
        @DecimalMin(value = "0.0", inclusive = true, message = "Maximum price must be non-negative.")
        BigDecimal maxPrice
) {
    // Các phương thức getter mặc định vẫn giữ nguyên
    public Integer index() {
        return Objects.requireNonNullElse(this.index, PaginationConstant.DEFAULT_PAGE_INDEX);
    }

    public String direction() {
        return Objects.requireNonNullElse(this.direction, PaginationConstant.DEFAULT_ORDER);
    }

    public String fields() {
        return Objects.requireNonNullElse(this.fields, PaginationConstant.DEFAULT_ORDER_BY);
    }

    public Short limit() {
        return Objects.requireNonNullElse(this.limit, PaginationConstant.DEFAULT_PAGE_SIZE);
    }

    public BigDecimal minPrice() {
        return Objects.requireNonNullElse(this.minPrice, BigDecimal.ZERO);
    }

    public BigDecimal maxPrice() {
        return Objects.requireNonNullElse(this.maxPrice, BigDecimal.ZERO);
    }

    // Phương thức validation cho origin sử dụng @AssertTrue
    @AssertTrue(message = "Invalid origin value(s). If provided, each must be one of: IMPORTED, LOCALLY_ASSEMBLED, DOMESTIC_MANUFACTURING (comma-separated).")
    public boolean isOriginValid() {
        if (this.origin == null || this.origin.trim().isEmpty()) {
            return true; // Cho phép null hoặc chuỗi rỗng. Nếu không muốn, bạn có thể thêm @NotBlank cho trường 'origin'.
        }

        String[] originValues = this.origin.split(",");
        for (String value : originValues) {
            String trimmedValue = value.trim();
            if (trimmedValue.isEmpty()) {
                // Bỏ qua các phần tử rỗng (ví dụ: từ "A,,B" hoặc " ,A")
                // Nếu chuỗi chỉ là "," hoặc "   ", trimmedOrigin sẽ là rỗng và đã return true ở trên.
                // Nếu chuỗi là "A, ,B", phần tử ở giữa sau trim sẽ là rỗng và được bỏ qua.
                continue;
            }
            try {
                Origin.valueOf(trimmedValue.toUpperCase()); // Enum của bạn là IMPORTED, LOCALLY_ASSEMBLED, DOMESTIC_MANUFACTURING
            } catch (IllegalArgumentException e) {
                return false; // Tìm thấy một giá trị không hợp lệ
            }
        }
        return true;
    }


    // Các phương thức tiện ích để parse chuỗi ID
    public List<Integer> getManufacturerIdsAsList() {
        if (manufacturerIds == null || manufacturerIds.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(manufacturerIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public List<Integer> getCarTypeIdsAsList() {
        if (carTypeIds == null || carTypeIds.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(carTypeIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public List<Integer> getCarSegmentIdsAsList() {
        if (carSegmentIds == null || carSegmentIds.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(carSegmentIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public List<Origin> getOriginsAsList() {
        if (origin == null || origin.trim().isEmpty()) {
            return List.of();
        }
        // Phương thức isOriginValid() đã kiểm tra tính hợp lệ của các enum.
        // Ở đây chỉ cần parse.
        return Arrays.stream(origin.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> Origin.valueOf(s.toUpperCase()))
                .collect(Collectors.toList());
    }
}