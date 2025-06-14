package web.car_system.Car_Service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.car_system.Car_Service.domain.dto.attribute.AttributeOnlyResponseDTO;
import web.car_system.Car_Service.domain.entity.Specification;

import java.util.List;
import java.util.Optional;

public interface SpecificationRepository extends JpaRepository<Specification, Integer> {
    Optional<Specification> findByName(String name);
    @Query(value = """
        SELECT 
            s.specification_id AS id,
            s.name AS name,
            a.attribute_id AS attributeId,
            a.name AS attributeName
        FROM specifications s
        LEFT JOIN (
            SELECT 
                attribute_id,
                name,
                specification_id,
                ROW_NUMBER() OVER (PARTITION BY specification_id ORDER BY attribute_id) AS rn
            FROM attributes
        ) a ON s.specification_id = a.specification_id AND a.rn <= 200
        ORDER BY s.specification_id, a.attribute_id
        """, nativeQuery = true)
    List<Object[]> findAllSpecificationsWithLimitedAttributes();

    @Query("SELECT new web.car_system.Car_Service.domain.dto.attribute.AttributeOnlyResponseDTO(a.attributeId, a.name) " +
            "FROM Attribute a WHERE a.specification.specificationId = :specId " +
            "AND (:keyword IS NULL OR :keyword = '' OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY a.attributeId")
    Page<AttributeOnlyResponseDTO> findAttributesBySpecificationIdAndOptionalKeyword(
            @Param("specId") Integer specId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT COUNT(a) FROM Attribute a")
    long countAttributes();
}
