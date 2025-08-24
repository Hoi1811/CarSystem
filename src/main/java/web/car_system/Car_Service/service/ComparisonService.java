package web.car_system.Car_Service.service;

import web.car_system.Car_Service.domain.dto.comparison.ComparisonResultDTO;

import java.util.List;

public interface ComparisonService {

    /**
     * So sánh một danh sách các xe dựa trên ID của chúng.
     * @param carIds Danh sách các ID của xe cần so sánh.
     * @return một đối tượng ComparisonResultDTO chứa kết quả so sánh chi tiết.
     */
    ComparisonResultDTO compareCars(List<Integer> carIds);
}