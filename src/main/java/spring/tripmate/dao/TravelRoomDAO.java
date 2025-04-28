package spring.tripmate.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import spring.tripmate.domain.mapping.RoomMember;

import java.util.List;
import java.util.Optional;

public interface TravelRoomDAO extends JpaRepository<RoomMember, Long> {

    Optional<RoomMember> findById(Long memberId);

    List<RoomMember> findByConsumerId(Long consumerId);

    List<RoomMember> findByRoomId(Long roomId);

    @Transactional
    void deleteByConsumerIdAndRoomId(Long consumerId, Long roomId);

    @Query("SELECT CASE WHEN COUNT(rm) > 0 THEN true ELSE false END FROM RoomMember rm WHERE rm.room.id = :roomId AND rm.member.id = :consumerId")
    boolean existsByRoomIdAndConsumerId(Long roomId, Long consumerId);
}

