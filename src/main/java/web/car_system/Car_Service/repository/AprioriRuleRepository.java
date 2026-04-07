package web.car_system.Car_Service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import web.car_system.Car_Service.domain.entity.AprioriRule;

import java.util.List;

@Repository
public interface AprioriRuleRepository extends JpaRepository<AprioriRule, Long> {
    
    /**
     * Find top N recommendations for a given car (ordered by confidence)
     * Uses JOIN FETCH to eagerly load Car entities and avoid LazyInitializationException
     * with soft-deleted cars (Car has @SQLRestriction("deleted_at IS NULL"))
     */
    @Query("SELECT r FROM AprioriRule r " +
           "JOIN FETCH r.antecedentCar " +
           "JOIN FETCH r.consequentCar " +
           "WHERE r.antecedentCar.carId = :carId " +
           "ORDER BY r.confidence DESC, r.lift DESC")
    List<AprioriRule> findTopRecommendationsForCar(@Param("carId") Integer carId);
    
    /**
     * Find all rules where given car is the consequence (reverse lookup)
     */
    @Query("SELECT r FROM AprioriRule r " +
           "JOIN FETCH r.antecedentCar " +
           "JOIN FETCH r.consequentCar " +
           "WHERE r.consequentCar.carId = :carId " +
           "ORDER BY r.confidence DESC")
    List<AprioriRule> findRulesWithConsequent(@Param("carId") Integer carId);
    
    /**
     * Check if a rule already exists (prevent duplicates)
     */
    boolean existsByAntecedentCar_CarIdAndConsequentCar_CarId(
            Integer antecedentCarId,
            Integer consequentCarId
    );
    
    /**
     * Delete all rules (before regenerating from Apriori algorithm)
     */
    void deleteAll();
}
