package web.car_system.Car_Service.service.advisor.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Serialize/deserialize vector float[] ↔ JSON string.
 * Dùng khi lưu vào cột LONGTEXT `embedding_json`.
 *
 * Tại sao chọn JSON thay vì binary BLOB?
 *  - Dễ debug bằng phpMyAdmin/CLI.
 *  - Migrate sang vector DB (pgvector, Qdrant) sau này dễ — chỉ cần decode JSON.
 */
public final class VectorJsonCodec {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<Float>> LIST_FLOAT = new TypeReference<>() {};

    private VectorJsonCodec() {}

    public static String encode(float[] vec) {
        if (vec == null) throw new IllegalArgumentException("vec must not be null");
        List<Float> list = new ArrayList<>(vec.length);
        for (float v : vec) list.add(v);
        try {
            return MAPPER.writeValueAsString(list);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encode vector to JSON", e);
        }
    }

    public static float[] decode(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("json must not be null/blank");
        }
        try {
            List<Float> list = MAPPER.readValue(json, LIST_FLOAT);
            float[] arr = new float[list.size()];
            for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
            return arr;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decode vector JSON", e);
        }
    }
}
