package spring.tripmate.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import spring.tripmate.domain.TravelPlan;

public interface TravelPlanDAO extends JpaRepository<TravelPlan, Long> {
    Page<TravelPlan> findByConsumerId(Long consumerId, PageRequest pageRequest);
    Page<TravelPlan> findByThemeContaining(@Param("theme") String theme, PageRequest pageRequest);

    @Query("SELECT p FROM TravelPlan p WHERE p.destination LIKE CONCAT('%', :country, '%')")
    Page<TravelPlan> findByCountry(@Param("country") String country, PageRequest pageRequest);
}
