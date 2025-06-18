package spring.tripmate.dao;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import spring.tripmate.domain.TravelRoom;

import java.util.Optional;

public interface TravelRoomDAO extends JpaRepository<TravelRoom, Long> {
    TravelRoom findByPlanId(Long planId);

    @Query("SELECT r FROM TravelRoom r JOIN FETCH r.plan WHERE r.id = :roomId")
    Optional<TravelRoom> findByIdWithPlan(@Param("roomId") Long roomId);

    @Query("SELECT r FROM TravelRoom r JOIN FETCH r.plan p JOIN FETCH p.places WHERE r.id = :roomId")
    Optional<TravelRoom> findByIdWithPlanWithPlaces(@Param("roomId") Long roomId);

}

