package spring.tripmate.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import spring.tripmate.domain.Consumer;
import spring.tripmate.domain.mapping.RoomMember;

import java.util.List;

public interface RoomMemberDAO extends JpaRepository<RoomMember, Long> {

	/*
	 * RoomMember findByConsumerId(Long consumerId);
	 * 
	 * List<RoomMember> findByRoomId(Long roomId);
	 * 
	 * @Query("SELECT rm.member FROM RoomMember rm WHERE rm.room.id = :roomId")
	 * List<Consumer> findConsumersByRoomId(Long roomId);
	 * 
	 * @Transactional void deleteByConsumerIdAndRoomId(Long consumerId, Long
	 * roomId);
	 * 
	 * @Query("SELECT CASE WHEN COUNT(rm) > 0 THEN true ELSE false END FROM RoomMember rm WHERE rm.room.id = :roomId AND rm.member.id = :consumerId"
	 * ) boolean existsByRoomIdAndConsumerId(Long roomId, Long consumerId);
	 */
}

