package web.car_system.Car_Service.service.advisor.util;

import web.car_system.Car_Service.domain.entity.Car;
import web.car_system.Car_Service.domain.entity.CarAttribute;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

/**
 * Build đoạn văn tự nhiên mô tả 1 xe — INPUT của Embedding API,
 * quyết định 80% chất lượng retrieval.
 *
 * Phiên bản v3 (cải tiến từ kết quả retrieval thực tế):
 *  - FIX duplicate manufacturer: car.name thường đã chứa mfg name (vd "Bentley Continental")
 *    nên không lặp "Xe Bentley Bentley Continental".
 *  - FRONT-LOAD signal mạnh ở 5 dòng đầu (segment, seats, price tier, use case) —
 *    embedding ưu tiên đầu input, signal đậm ở đó kéo retrieval đúng theme.
 *  - PRICE TIER bằng tiếng Việt thường ngày: "phổ thông", "trung cấp", "cao cấp", "xe sang", "siêu sang"
 *    → match tự nhiên với từ user dùng.
 *  - WHITELIST attribute quan trọng (~30 attr) thay vì dump 90+ — giảm dilution của
 *    các attr ít quan trọng (đèn, treo, lốp...) khiến embedding bị nhiễu.
 *
 * Method buildSourceTextHash() sinh SHA-256 dùng để skip re-embed khi xe không đổi.
 */
public final class CarTextBuilder {

    private CarTextBuilder() {}

    /**
     * Whitelist attribute quan trọng cho embedding. Attribute không match sẽ bị bỏ
     * để giảm dilution. Tên match (case-insensitive) một phần — vd "Số chỗ" match cả
     * "Số chỗ ngồi", "số chỗ".
     */
    private static final Set<String> WHITELIST_ATTRS_KEYWORDS = Set.of(
            "số chỗ",
            "kiểu động cơ",
            "loại nhiên liệu",
            "dung tích",
            "công suất",
            "hộp số",
            "hệ dẫn động",
            "tiêu thụ nhiên liệu",
            "mức tiêu thụ",
            "số túi khí",
            "túi khí",
            "khoảng sáng gầm",
            "kích thước",
            "chiều dài cơ sở",
            "chất liệu bọc ghế",
            "điều hòa",
            "điều hoà",
            "màn hình giải trí",
            "phanh trước",
            "phanh sau",
            "cân bằng điện tử",
            "chống bó cứng phanh",
            "abs",
            "esp",
            "vsc",
            "cruise control",
            "ga tự động",
            "cảnh báo điểm mù",
            "cảnh báo va chạm",
            "phanh tự động",
            "camera 360"
    );

    /**
     * @param car             Entity Car đã được attach (có manufacturer + segment).
     * @param attributesOfCar List CarAttribute của RIÊNG xe này (caller tự group).
     */
    public static String build(Car car, List<CarAttribute> attributesOfCar) {
        StringBuilder sb = new StringBuilder(1500);

        String manufacturerName = car.getManufacturer() != null ? safe(car.getManufacturer().getName()) : "";
        String segmentName = car.getCarSegment() != null ? safe(car.getCarSegment().getName()) : "";
        String name = safe(car.getName());
        String model = safe(car.getModel());
        BigDecimal price = car.getPrice();
        Integer seats = car.getSeats();
        String priceTier = priceTierLabel(price);
        String useCase = inferUseCase(segmentName, price, seats);

        // ==================== HEADER (front-loaded — embedding chú trọng đầu) ====================
        // Nhân đôi tên xe để tăng signal khi user gõ tên hãng:
        sb.append(buildFullName(manufacturerName, name, model)).append(", đời ").append(car.getYear()).append(".\n");

        // Lặp segment + seats + price 2 lần ở đầu (signal khuếch đại):
        if (!segmentName.isEmpty()) {
            sb.append("Phân khúc ").append(segmentName).append(". ");
        }
        if (seats != null) {
            sb.append(seats).append(" chỗ ngồi. ");
        }
        sb.append("Giá ").append(formatPrice(price)).append(" — ").append(priceTier).append(".\n");

        // Use case tag (gia đình / doanh nhân / off-road / ...):
        if (!useCase.isEmpty()) {
            sb.append("Phù hợp: ").append(useCase).append(".\n");
        }

        // ==================== BODY: spec ngắn từ Car entity ====================
        sb.append("Thông số: ");
        if (car.getTransmissionType() != null && !car.getTransmissionType().isBlank())
            sb.append("hộp số ").append(car.getTransmissionType()).append(", ");
        if (car.getDriveTrain() != null && !car.getDriveTrain().isBlank())
            sb.append("dẫn động ").append(car.getDriveTrain()).append(", ");
        if (car.getHorsepower() != null) sb.append(car.getHorsepower()).append(" mã lực, ");
        if (car.getFuelConsumption() != null)
            sb.append("tiêu hao ").append(car.getFuelConsumption()).append(" L/100km, ");
        if (car.getAirbagCount() != null) sb.append(car.getAirbagCount()).append(" túi khí, ");
        if (Boolean.TRUE.equals(car.getHasSunroof())) sb.append("có cửa sổ trời, ");
        // Bỏ engine_type ở đây vì 97% NULL trong DB — dùng attribute "Kiểu động cơ" thay
        trimTrailingComma(sb);
        sb.append(".\n");

        // ==================== ATTRIBUTE WHITELIST ====================
        if (attributesOfCar != null) {
            sb.append("Chi tiết: ");
            int kept = 0;
            for (CarAttribute ca : attributesOfCar) {
                if (ca.getAttribute() == null) continue;
                String attrName = ca.getAttribute().getName();
                String value = ca.getValue();
                if (attrName == null || attrName.isBlank() || value == null || value.isBlank()) continue;
                if (!isWhitelisted(attrName)) continue;
                sb.append(attrName).append(": ").append(value.trim()).append(". ");
                kept++;
            }
            if (kept == 0) {
                sb.setLength(sb.length() - "Chi tiết: ".length()); // remove unused header
            }
        }

        return sb.toString();
    }

    public static String buildSourceTextHash(String sourceText) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(sourceText.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    // ==================== Helpers ====================

    /**
     * Build "Xe [Mfg] [Name] [Model]" nhưng SKIP mfg nếu name đã bắt đầu bằng nó.
     * Vd: name="Bentley Continental", mfg="Bentley" → "Xe Bentley Continental GT S"
     *      name="Han Performance", mfg="BYD" → "Xe BYD Han Performance"
     */
    private static String buildFullName(String mfg, String name, String model) {
        StringBuilder n = new StringBuilder("Xe ");
        boolean nameStartsWithMfg = !mfg.isEmpty() && !name.isEmpty()
                && name.toLowerCase().startsWith(mfg.toLowerCase());
        if (!nameStartsWithMfg && !mfg.isEmpty()) {
            n.append(mfg).append(" ");
        }
        if (!name.isEmpty()) n.append(name);
        if (!model.isEmpty()) n.append(" ").append(model);
        return n.toString();
    }

    private static boolean isWhitelisted(String attrName) {
        String lower = attrName.toLowerCase();
        for (String kw : WHITELIST_ATTRS_KEYWORDS) {
            if (lower.contains(kw)) return true;
        }
        return false;
    }

    private static void trimTrailingComma(StringBuilder sb) {
        while (sb.length() > 0 && (sb.charAt(sb.length() - 1) == ' ' || sb.charAt(sb.length() - 1) == ',')) {
            sb.setLength(sb.length() - 1);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String formatPrice(BigDecimal price) {
        if (price == null) return "chưa rõ";
        BigDecimal billions = price.movePointLeft(9);
        if (billions.compareTo(BigDecimal.ONE) >= 0) {
            return billions.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + " tỷ đồng";
        }
        BigDecimal millions = price.movePointLeft(6);
        return millions.setScale(0, RoundingMode.HALF_UP).toPlainString() + " triệu đồng";
    }

    /**
     * Bucket giá theo từ user thường dùng. Confirmed bằng phân bố thực:
     *  - 16% xe < 600M, 31% 600M-1B, 19% 1-2B, 19% 2-5B, 16% > 5B.
     */
    private static String priceTierLabel(BigDecimal price) {
        if (price == null) return "chưa rõ giá";
        BigDecimal m = price.movePointLeft(6); // VND → triệu
        if (m.compareTo(BigDecimal.valueOf(600)) < 0) return "phân khúc phổ thông giá rẻ dưới 600 triệu";
        if (m.compareTo(BigDecimal.valueOf(1000)) < 0) return "phân khúc phổ thông giá tầm trung 600 triệu đến 1 tỷ";
        if (m.compareTo(BigDecimal.valueOf(2000)) < 0) return "phân khúc trung cao cấp 1 đến 2 tỷ";
        if (m.compareTo(BigDecimal.valueOf(5000)) < 0) return "phân khúc cao cấp xe sang 2 đến 5 tỷ";
        return "phân khúc siêu sang trên 5 tỷ";
    }

    private static String inferUseCase(String segmentName, BigDecimal price, Integer seats) {
        if (segmentName == null) segmentName = "";
        String s = segmentName.toLowerCase();
        StringBuilder uc = new StringBuilder();

        if (s.contains("suv") || s.contains("mpv")) {
            uc.append("xe gia đình đông người");
            if (seats != null && seats >= 7) uc.append(", chở 7 chỗ trở lên");
            uc.append(", đi xa, đường xấu, dã ngoại cuối tuần");
        } else if (s.contains("hạng b") || s.contains("hạng a")) {
            uc.append("gia đình trẻ, người mới đi làm văn phòng, di chuyển nội đô, tiết kiệm xăng");
        } else if (s.contains("hạng c")) {
            uc.append("gia đình 4 đến 5 người, đi lại đa năng phố và đường dài");
        } else if (s.contains("hạng d") || s.contains("hạng e") || s.contains("xe sang")) {
            uc.append("doanh nhân, công tác, tiếp khách, ưu tiên không gian sang trọng");
        } else if (s.contains("siêu sang")) {
            uc.append("đẳng cấp tỷ phú, tiếp đối tác cấp cao");
        } else if (s.contains("siêu xe") || s.contains("thể thao")) {
            uc.append("đam mê tốc độ, xe thể thao, dành cho người chơi sưu tập");
        } else if (s.contains("bán tải") || s.contains("pickup")) {
            uc.append("công việc, chở hàng, off-road, địa hình khó");
        } else {
            uc.append("nhu cầu di chuyển đa năng");
        }

        if (seats != null) {
            if (seats <= 2) uc.append(", xe 2 chỗ");
            else if (seats == 4) uc.append(", xe 4 chỗ");
            else if (seats == 5) uc.append(", xe 5 chỗ");
            else if (seats == 6) uc.append(", xe 6 chỗ");
            else if (seats == 7) uc.append(", xe 7 chỗ rộng rãi");
            else if (seats >= 8) uc.append(", xe trên 8 chỗ chở đông người");
        }
        return uc.toString();
    }
}
