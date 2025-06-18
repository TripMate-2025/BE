package spring.tripmate.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import spring.tripmate.domain.Consumer;
import spring.tripmate.domain.mapping.RoomMember;
import spring.tripmate.domain.mapping.RoomMemberId;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomMemberDAO extends JpaRepository<RoomMember, RoomMemberId> {

	Optional<RoomMember> findById(RoomMemberId memberId);

	RoomMember findById_ConsumerId(Long consumerId);

	List<RoomMember> findById_RoomId(Long roomId);

	@Query("SELECT rm FROM RoomMember rm WHERE rm.member.id = :memberId")
	List<RoomMember> findByMemberId(@Param("memberId") Long memberId);

	@Query("SELECT rm.member FROM RoomMember rm WHERE rm.id.roomId = :roomId")
	List<Consumer> findConsumersByRoomId(Long roomId);

	@Transactional
	void deleteById_ConsumerIdAndId_RoomId(Long consumerId, Long roomId);

	@Query("SELECT CASE WHEN COUNT(rm) > 0 THEN true ELSE false END FROM RoomMember rm WHERE rm.id.roomId = :roomId AND rm.id.consumerId = :consumerId")
	boolean existsByRoomIdAndConsumerId(Long roomId, Long consumerId);
}

