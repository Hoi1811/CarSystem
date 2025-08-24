package web.car_system.Car_Service.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import web.car_system.Car_Service.domain.entity.AttributeEnumOrder;
import web.car_system.Car_Service.domain.entity.AttributeEnumOrderId;


import java.util.List;

@Repository
public interface AttributeEnumOrderRepository extends JpaRepository<AttributeEnumOrder, AttributeEnumOrderId> {

    /**
     * Tìm tất cả các lựa chọn (options) cho một thuộc tính dựa trên tên của thuộc tính đó.
     * Sắp xếp theo thứ hạng (rank) để hiển thị trên UI một cách có trật tự.
     * @param attributeName Tên của thuộc tính (ví dụ: "Hộp số", "Loại nhiên liệu")
     * @return Danh sách các lựa chọn được sắp xếp.
     */
    @Query("SELECT aeo FROM AttributeEnumOrder aeo " +
            "JOIN aeo.attribute a " +
            "WHERE a.name = :attributeName " +
            "ORDER BY aeo.rank ASC")
    List<AttributeEnumOrder> findByAttributeNameOrderByRank(String attributeName);

    /**
     * (Tùy chọn) Một cách truy vấn khác dựa vào ID của thuộc tính, có thể hữu ích trong một số trường hợp.
     * @param attributeId ID của thuộc tính.
     * @return Danh sách các lựa chọn được sắp xếp.
     */
    List<AttributeEnumOrder> findById_AttributeIdOrderByRankAsc(Integer attributeId);
}
