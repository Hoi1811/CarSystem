package web.car_system.Car_Service.service.advisor.util;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse hard constraint từ truy vấn tự nhiên tiếng Việt:
 *  - "ngân sách 1.2 tỷ"      → maxPrice = 1_200_000_000
 *  - "tầm 600 triệu"          → maxPrice = 600_000_000
 *  - "dưới 800 triệu"         → maxPrice = 800_000_000
 *  - "khoảng 2 tỷ"            → maxPrice = 2_000_000_000
 *  - "1.5 tỷ - 2 tỷ"          → maxPrice = 2_000_000_000
 *  - "xe 7 chỗ", "9 chỗ"      → minSeats = 7 (hoặc 9)
 *
 * Triết lý: regex chỉ match khi có CHỮ SỐ + đơn vị/từ khoá rõ ràng.
 * Nếu không match → trả empty, fallback về cosine thuần.
 */
public final class QueryConstraintExtractor {

    private QueryConstraintExtractor() {}

    /** Tolerance cho price filter — vd budget 600M cho phép retrieve đến 720M. */
    private static final double PRICE_TOLERANCE = 1.20;

    // ==================== Patterns (chạy trên text ĐÃ NORMALIZE bỏ dấu) ====================
    // Đầu vào đã được `normalize()` xử lý: "xe điện 1 tỷ" → "xe dien 1 ty"
    // → tất cả pattern dùng ký tự ASCII không dấu.

    /** Số (kèm decimal "." hoặc ",") + đơn vị tỷ. */
    private static final Pattern PRICE_BIL = Pattern.compile(
            "([0-9]+(?:[.,][0-9]+)?)\\s*(?:ty|ti)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern PRICE_MIL = Pattern.compile(
            "([0-9]+(?:[.,][0-9]+)?)\\s*(?:trieu|tr|m)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern SEATS = Pattern.compile(
            "([0-9]+)\\s*cho", Pattern.CASE_INSENSITIVE);

    /**
     * Match "xe dien", "ev", "electric", "thuan dien", "pure ev". Chạy trên text đã bỏ dấu.
     */
    private static final Pattern EV_INTENT = Pattern.compile(
            "\\b(xe\\s*dien|thuan\\s*dien|pure\\s*ev|electric|ev)\\b",
            Pattern.CASE_INSENSITIVE);

    /** Match "hybrid" / "lai" / "hev" / "phev" — exclude khỏi EV intent. */
    private static final Pattern HYBRID_INTENT = Pattern.compile(
            "\\b(hybrid|xe\\s*lai|hev|phev)\\b", Pattern.CASE_INSENSITIVE);

    /**
     * Trả về maxPrice đã nhân tolerance (vnd). Lấy GIÁ TRỊ LỚN NHẤT trong query
     * (khi user nói "1.5 tỷ - 2 tỷ" → lấy 2 tỷ làm trần).
     */
    public static Optional<BigDecimal> extractMaxPrice(String query) {
        if (query == null || query.isBlank()) return Optional.empty();
        String q = normalize(query);

        BigDecimal max = null;

        Matcher mBil = PRICE_BIL.matcher(q);
        while (mBil.find()) {
            BigDecimal v = parseDecimal(mBil.group(1)).multiply(BigDecimal.valueOf(1_000_000_000L));
            if (max == null || v.compareTo(max) > 0) max = v;
        }

        Matcher mMil = PRICE_MIL.matcher(q);
        while (mMil.find()) {
            BigDecimal v = parseDecimal(mMil.group(1)).multiply(BigDecimal.valueOf(1_000_000L));
            if (max == null || v.compareTo(max) > 0) max = v;
        }

        if (max == null) return Optional.empty();
        BigDecimal withTolerance = max.multiply(BigDecimal.valueOf(PRICE_TOLERANCE));
        return Optional.of(withTolerance);
    }

    /**
     * "X chỗ" → X. Trả empty nếu không có.
     * Heuristic: user nói "7 chỗ" thường nghĩa là "≥ 7 chỗ" (bao gồm cả 8, 9 chỗ).
     */
    public static Optional<Integer> extractMinSeats(String query) {
        if (query == null || query.isBlank()) return Optional.empty();
        Matcher m = SEATS.matcher(normalize(query));
        if (m.find()) {
            try {
                int seats = Integer.parseInt(m.group(1));
                if (seats >= 2 && seats <= 16) {
                    return Optional.of(seats);
                }
            } catch (NumberFormatException ignored) {
                // skip
            }
        }
        return Optional.empty();
    }

    /**
     * True nếu user muốn xe điện (pure EV) — không bao gồm hybrid.
     */
    public static boolean wantsPureEv(String query) {
        if (query == null || query.isBlank()) return false;
        String q = normalize(query);
        // Nếu có "hybrid" → user nói rõ muốn hybrid, không apply EV filter
        if (HYBRID_INTENT.matcher(q).find()) return false;
        return EV_INTENT.matcher(q).find();
    }

    private static BigDecimal parseDecimal(String s) {
        return new BigDecimal(s.replace(',', '.'));
    }

    /**
     * Normalize Vietnamese query: bỏ dấu thanh + dấu mũ, lowercase, tách "đ" → "d".
     * Cho phép user gõ kiểu "xe dien 1 ty" hoặc "xe điện 1 tỷ" — đều match như nhau.
     *
     * Ví dụ:
     *  - "Xe điện 1 tỷ"  → "xe dien 1 ty"
     *  - "ngân sách 600 triệu" → "ngan sach 600 trieu"
     *  - "MPV 7 chỗ"     → "mpv 7 cho"
     */
    private static String normalize(String text) {
        // Step 1: NFD decompose tách ký tự thành base + combining marks
        String nfd = Normalizer.normalize(text, Normalizer.Form.NFD);
        // Step 2: bỏ tất cả combining marks (dấu thanh, dấu mũ)
        String stripped = nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        // Step 3: "đ" / "Đ" không có decomposition trong NFD → thay tay
        stripped = stripped.replace('đ', 'd').replace('Đ', 'D');
        return stripped.toLowerCase();
    }
}
