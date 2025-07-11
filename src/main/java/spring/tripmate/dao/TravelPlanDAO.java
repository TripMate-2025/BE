package spring.tripmate.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import spring.tripmate.domain.TravelPlan;

import java.util.Optional;

public interface TravelPlanDAO extends JpaRepository<TravelPlan, Long> {
    Page<TravelPlan> findByConsumerId(Long consumerId, Pageable pageable);
    Page<TravelPlan> findByThemeContaining(@Param("theme") String theme, Pageable pageable);

    @Query("SELECT DISTINCT p FROM TravelPlan p LEFT JOIN FETCH p.posts WHERE p.destination LIKE CONCAT('%', :country, '%')")
    Page<TravelPlan> findByCountryWithPosts(@Param("country") String country, Pageable pageable);

    @Query("SELECT p FROM TravelPlan p LEFT JOIN FETCH p.places WHERE p.id = :planId")
    Optional<TravelPlan> findByIdWithPlaces(@Param("planId") Long planId);
}
