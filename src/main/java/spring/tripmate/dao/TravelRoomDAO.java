package spring.tripmate.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.tripmate.domain.TravelRoom;

public interface TravelRoomDAO extends JpaRepository<TravelRoom, Long> {
    TravelRoom findByPlanId(Long planId);
}

