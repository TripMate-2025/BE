package spring.tripmate.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import spring.tripmate.domain.TravelPlace;

public interface TravelPlaceDAO extends JpaRepository<TravelPlace, Long> {
    Page<TravelPlace> findByPlanId(Long planId, PageRequest pageRequest);
}
