package spring.tripmate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.tripmate.converter.TravelRoomConverter;
import spring.tripmate.dao.ConsumerDAO;
import spring.tripmate.dao.RoomMemberDAO;
import spring.tripmate.dao.TravelPlanDAO;
import spring.tripmate.dao.TravelRoomDAO;
import spring.tripmate.domain.Consumer;
import spring.tripmate.domain.TravelPlan;
import spring.tripmate.domain.TravelRoom;
import spring.tripmate.domain.apiPayload.code.status.ErrorStatus;
import spring.tripmate.domain.apiPayload.exception.handler.RoomHandler;
import spring.tripmate.domain.apiPayload.exception.handler.PlanHandler;
import spring.tripmate.domain.apiPayload.exception.handler.UnauthorizedException;
import spring.tripmate.domain.mapping.RoomMember;
import spring.tripmate.domain.mapping.RoomMemberId;
import spring.tripmate.dto.ConsumerResponseDTO;
import spring.tripmate.dto.TravelRoomResponseDTO;
import spring.tripmate.security.JwtProvider;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelRoomService {

    private final TravelRoomDAO roomDAO;
    private final TravelPlanDAO planDAO;
    private final ConsumerDAO consumerDAO;
    private final RoomMemberDAO memberDAO;
    private final JwtProvider jwtProvider;

    @Transactional
    public TravelRoomResponseDTO.RoomDTO createRoom(Long planId, String authHeader) {
        Consumer owner = getConsumerFromHeader(authHeader);

        TravelPlan plan = planDAO.findByIdWithPlaces(planId)
                .orElseThrow(() -> new PlanHandler(ErrorStatus.PLAN_NOT_FOUND));

        if (plan.getPlaces() == null || plan.getPlaces().isEmpty()) {
            throw new PlanHandler(ErrorStatus.PLAN_NOT_FOUND);
        }

        TravelRoom existingRoom = roomDAO.findByPlanId(planId);

        if (existingRoom != null) {
            boolean isAlreadyMember = existingRoom.getMembers().stream()
                    .anyMatch(rm -> rm.getMember().getId().equals(owner.getId()));
            System.out.println("[LOG] isAlreadyMember: " + isAlreadyMember);

            if (!isAlreadyMember) {
                RoomMember member = RoomMember.builder()
                        .id(new RoomMemberId(owner.getId(), existingRoom.getId()))
                        .member(owner)
                        .room(existingRoom)
                        .build();
                memberDAO.save(member);
                System.out.println("[LOG] Added new member to existing room");
            }

            List<Consumer> members = existingRoom.getMembers().stream()
                    .map(RoomMember::getMember)
                    .collect(Collectors.toList());

            return TravelRoomConverter.toRoomDTO(existingRoom, members);
        }

        TravelRoom newRoom = TravelRoom.builder()
                .name(plan.getTitle())
                .plan(plan)
                .build();

        System.out.println("[LOG] New room before save: " + newRoom);
        roomDAO.saveAndFlush(newRoom);
        System.out.println("[LOG] New room after save: " + newRoom + ", id: " + newRoom.getId());

        RoomMember member = RoomMember.builder()
                .id(new RoomMemberId(owner.getId(), newRoom.getId()))
                .member(owner)
                .room(newRoom)
                .build();
        memberDAO.save(member);
        System.out.println("[LOG] Added new member to new room");

        return TravelRoomConverter.toRoomDTO(newRoom, List.of(owner));
    }


    @Transactional(readOnly = true)
    public TravelRoomResponseDTO.RoomDTO getRoom(Long roomId) {
        TravelRoom room = roomDAO.findById(roomId)
                .orElseThrow(() -> new RoomHandler(ErrorStatus.PLAN_NOT_FOUND));
        List<Consumer> members = room.getMembers().stream()
                .map(RoomMember::getMember)
                .collect(Collectors.toList());
        return TravelRoomConverter.toRoomDTO(room, members);
    }

    @Transactional
    public ConsumerResponseDTO.RoomMembersDTO addMember(Long roomId, String authHeader) {
        Consumer consumer = getConsumerFromHeader(authHeader);

        TravelRoom room = roomDAO.findById(roomId)
                .orElseThrow(() -> new RoomHandler(ErrorStatus.PLAN_NOT_FOUND));

        // 이미 등록된 멤버인지 확인
        RoomMemberId memberId = new RoomMemberId(consumer.getId(), room.getId());
        if (memberDAO.findById(memberId).isPresent()) {
            throw new RoomHandler(ErrorStatus.ALREADY_ROOM_MEMBER);
        }

        RoomMember member = RoomMember.builder()
                .id(memberId)
                .member(consumer)
                .room(room)
                .build();
        memberDAO.save(member);

        List<ConsumerResponseDTO.RoomMembersDTO.MemberDTO> dtos = room.getMembers().stream()
                .map(RoomMember::getMember)
                .map(c -> {
                    ConsumerResponseDTO.RoomMembersDTO.MemberDTO m = new ConsumerResponseDTO.RoomMembersDTO.MemberDTO();
                    m.setMemberId(c.getId());
                    m.setEmail(c.getEmail());
                    m.setProfile(c.getProfile());
                    m.setUsername(c.getNickname());
                    return m;
                })
                .collect(Collectors.toList());

        return new ConsumerResponseDTO.RoomMembersDTO(dtos);
    }

    private Consumer getConsumerFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException(ErrorStatus.INVALID_AUTH_HEADER);
        }
        String token = authHeader.replace("Bearer ", "");
        String email = jwtProvider.getEmailFromToken(token);
        Consumer consumer = consumerDAO.findByEmail(email);
        if (consumer == null) {
            throw new UnauthorizedException(ErrorStatus.CONSUMER_NOT_FOUND);
        }
        return consumer;
    }

    @Transactional(readOnly = true)
    public TravelRoom findRoomEntityById(Long roomId) {
        return roomDAO.findById(roomId)
                .orElseThrow(() -> new RoomHandler(ErrorStatus.PLAN_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<TravelRoomResponseDTO.RoomDTO> getRoomsForConsumer(String authHeader) {
        Consumer consumer = getConsumerFromHeader(authHeader);

        List<RoomMember> memberships = memberDAO.findByMemberId(consumer.getId());
        List<TravelRoom> rooms = memberships.stream()
                .map(RoomMember::getRoom)
                .toList();

        return rooms.stream()
                .map(room -> {
                    List<Consumer> members = room.getMembers().stream()
                            .map(RoomMember::getMember)
                            .toList();
                    return TravelRoomConverter.toRoomDTO(room, members);
                })
                .toList();
    }

}
