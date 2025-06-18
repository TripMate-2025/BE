package spring.tripmate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.tripmate.client.GooglePlaceClient;
import spring.tripmate.converter.TravelRoomConverter;
import spring.tripmate.dao.ConsumerDAO;
import spring.tripmate.dao.RoomMemberDAO;
import spring.tripmate.dao.TravelPlanDAO;
import spring.tripmate.dao.TravelRoomDAO;
import spring.tripmate.domain.Consumer;
import spring.tripmate.domain.TravelPlace;
import spring.tripmate.domain.TravelPlan;
import spring.tripmate.domain.TravelRoom;
import spring.tripmate.domain.apiPayload.code.status.ErrorStatus;
import spring.tripmate.domain.apiPayload.exception.handler.InvalidGooglePlaceException;
import spring.tripmate.domain.apiPayload.exception.handler.RoomHandler;
import spring.tripmate.domain.apiPayload.exception.handler.PlanHandler;
import spring.tripmate.domain.apiPayload.exception.handler.UnauthorizedException;
import spring.tripmate.domain.enums.CategoryType;
import spring.tripmate.domain.mapping.RoomMember;
import spring.tripmate.domain.mapping.RoomMemberId;
import spring.tripmate.dto.ConsumerResponseDTO;
import spring.tripmate.dto.GooglePlaceResponseDTO;
import spring.tripmate.dto.PlanRequestDTO;
import spring.tripmate.dto.TravelRoomResponseDTO;
import spring.tripmate.security.JwtProvider;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelRoomService {

    private final TravelRoomDAO roomDAO;
    private final TravelPlanDAO planDAO;
    private final ConsumerDAO consumerDAO;
    private final RoomMemberDAO memberDAO;
    private final JwtProvider jwtProvider;
    private final GooglePlaceClient googlePlaceClient;

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

    @Transactional
    public void deletePlace(Long roomId, Long placeId, String authHeader) {
        System.out.println("[DEBUG] deletePlace 시작 - roomId: " + roomId + ", placeId: " + placeId);

        Consumer requester = getConsumerFromHeader(authHeader);
        System.out.println("[DEBUG] 인증된 사용자 ID: " + requester.getId());

        // 🔍 roomId로 room 조회 (plan도 같이 fetch)
        TravelRoom room = roomDAO.findByIdWithPlan(roomId)
                .orElseThrow(() -> {
                    System.out.println("[ERROR] 방을 찾을 수 없음 - roomId: " + roomId);
                    return new PlanHandler(ErrorStatus.ROOM_NOT_FOUND);
                });
        System.out.println("[DEBUG] 방 조회 완료 - 방 이름: " + room.getName());

        // 🔐 권한 확인
        boolean isMember = room.getMembers().stream()
                .anyMatch(m -> m.getMember().getId().equals(requester.getId()));

        if (!isMember) {
            System.out.println("[ERROR] 권한 없음 - 사용자 ID: " + requester.getId() + "는 방 멤버가 아님");
            throw new PlanHandler(ErrorStatus.PLAN_NOT_FOUND);
        }
        System.out.println("[DEBUG] 권한 확인 완료 - 멤버임");

        // 🧭 room에서 plan 가져오기
        TravelPlan plan = room.getPlan();
        if (plan == null) {
            System.out.println("[ERROR] 플랜이 없음 - roomId: " + roomId);
            throw new PlanHandler(ErrorStatus.PLAN_NOT_FOUND);
        }
        System.out.println("[DEBUG] 플랜 조회 완료 - 플랜 제목: " + plan.getTitle());

        // 🧹 place 제거
        boolean removed = plan.getPlaces().removeIf(place -> place.getId().equals(placeId));
        if (!removed) {
            System.out.println("[ERROR] 삭제할 장소를 찾을 수 없음 - placeId: " + placeId);
            throw new PlanHandler(ErrorStatus.PLACE_NOT_FOUND);
        }
        System.out.println("[DEBUG] 장소 삭제 완료 - placeId: " + placeId);

        planDAO.save(plan);  // 변경 사항 반영
        System.out.println("[DEBUG] 플랜 저장 완료");
    }

    @Transactional
    public Map<String, Object> updatePlaceInRoom(Long roomId, Map<String, Object> updatedFields, String authHeader) {
        System.out.println("[DEBUG] updatePlaceInRoom 시작 - roomId: " + roomId);

        Consumer requester = getConsumerFromHeader(authHeader);

        TravelRoom room = roomDAO.findByIdWithPlanWithPlaces(roomId)
                .orElseThrow(() -> new PlanHandler(ErrorStatus.ROOM_NOT_FOUND));

        boolean isMember = room.getMembers().stream()
                .anyMatch(m -> m.getMember().getId().equals(requester.getId()));

        if (!isMember) throw new PlanHandler(ErrorStatus.PLAN_NOT_FOUND);

        TravelPlan plan = room.getPlan();
        if (plan == null) throw new PlanHandler(ErrorStatus.PLAN_NOT_FOUND);

        // placeId는 updatedFields에서 꺼내야 함 (Long 타입)
        long placeId;
        Object placeIdObj = updatedFields.get("placeId");
        if (placeIdObj instanceof Number) {
            placeId = ((Number) placeIdObj).longValue();
        } else if (placeIdObj instanceof String) {
            placeId = Long.parseLong((String) placeIdObj);
        } else {
            throw new PlanHandler(ErrorStatus.PLACE_NOT_FOUND);
        }

        TravelPlace place = plan.getPlaces().stream()
                .filter(p -> p.getId().equals(placeId))
                .findFirst()
                .orElseThrow(() -> new PlanHandler(ErrorStatus.PLACE_NOT_FOUND));

        // 필드 업데이트 (null 체크 후 타입 변환 필요)
        if (updatedFields.containsKey("name")) {
            place.setName((String) updatedFields.get("name"));
        }
        if (updatedFields.containsKey("description")) {
            place.setDescription((String) updatedFields.get("description"));
        }
        if (updatedFields.containsKey("category")) {
            // category는 Enum이므로 변환 필요
            String catStr = (String) updatedFields.get("category");
            if (catStr != null) {
                place.setCategory(CategoryType.valueOf(catStr));
            }
        }
        if (updatedFields.containsKey("address")) {
            place.setAddress((String) updatedFields.get("address"));
        }
        if (updatedFields.containsKey("latitude")) {
            place.setLatitude(Double.valueOf(updatedFields.get("latitude").toString()));
        }
        if (updatedFields.containsKey("longitude")) {
            place.setLongitude(Double.valueOf(updatedFields.get("longitude").toString()));
        }
        if (updatedFields.containsKey("time")) {
            // 시간은 LocalDateTime으로 변환해야 함
            Object timeObj = updatedFields.get("time");
            if (timeObj instanceof String) {
                place.setTime(LocalDateTime.parse((String) timeObj));
            }
        }
        if (updatedFields.containsKey("dayNumber")) {
            place.setDayNumber(Integer.valueOf(updatedFields.get("dayNumber").toString()));
        }

        // 좌표나 주소가 변경된 경우 구글 좌표 업데이트 호출
        if (updatedFields.containsKey("name") || updatedFields.containsKey("address")) {
            // 직접 PlaceDTO처럼 새로 만들 필요 없고, 임시 DTO나 Map을 넘기거나 메서드를 수정해서 사용해도 됨.
            // 여기서는 간단히 재사용 편하게 Map을 PlaceDTO로 변환하는 보조 메서드 권장
            PlanRequestDTO.UpdateDTO.PlaceDTO tempDto = convertMapToPlaceDTO(updatedFields);
            updateCoordinatesAndAddress(place, plan, tempDto);
        }

        planDAO.save(plan);

        Map<String, Object> responseFields = new HashMap<>();
        responseFields.put("placeId", place.getId());
        responseFields.put("name", place.getName());
        responseFields.put("description", place.getDescription());
        responseFields.put("category", place.getCategory());
        responseFields.put("address", place.getAddress());
        responseFields.put("latitude", place.getLatitude());
        responseFields.put("longitude", place.getLongitude());
        responseFields.put("time", place.getTime());
        responseFields.put("dayNumber", place.getDayNumber());

        System.out.println("[DEBUG] 장소 수정 및 저장 완료 - placeId: " + place.getId());

        return responseFields;
    }

    // Map<String, Object> → PlaceDTO 변환 보조 메서드 (간단 구현 예)
    private PlanRequestDTO.UpdateDTO.PlaceDTO convertMapToPlaceDTO(Map<String, Object> map) {
        PlanRequestDTO.UpdateDTO.PlaceDTO dto = new PlanRequestDTO.UpdateDTO.PlaceDTO();

        if (map.containsKey("placeId")) {
            Object val = map.get("placeId");
            if (val instanceof Number) dto.setPlaceId(((Number) val).longValue());
            else if (val instanceof String) dto.setPlaceId(Long.parseLong((String) val));
        }
        if (map.containsKey("name")) dto.setName((String) map.get("name"));
        if (map.containsKey("description")) dto.setDescription((String) map.get("description"));
        if (map.containsKey("category")) {
            String catStr = (String) map.get("category");
            if (catStr != null) dto.setCategory(CategoryType.valueOf(catStr));
        }
        if (map.containsKey("address")) dto.setAddress((String) map.get("address"));
        if (map.containsKey("latitude")) dto.setLatitude(Double.valueOf(map.get("latitude").toString()));
        if (map.containsKey("longitude")) dto.setLongitude(Double.valueOf(map.get("longitude").toString()));
        if (map.containsKey("time")) {
            String timeStr = (String) map.get("time");
            dto.setTime(LocalDateTime.parse(timeStr));
        }
//        if (map.containsKey("dayNumber")) dto.setDayNumber(Integer.valueOf(map.get("dayNumber").toString()));

        return dto;
    }

    private void updateCoordinatesAndAddress(TravelPlace place, TravelPlan plan, PlanRequestDTO.UpdateDTO.PlaceDTO updatedPlaceDto) {
        StringBuilder queryBuilder = new StringBuilder();

        // TravelPlan의 destination (예: "Seoul, South Korea") 추가
        if (plan.getDestination() != null && !plan.getDestination().isBlank()) {
            queryBuilder.append(plan.getDestination().trim()).append(" ");
        }

        // 장소 이름 추가
        if (updatedPlaceDto.getName() != null && !updatedPlaceDto.getName().isBlank()) {
            queryBuilder.append(updatedPlaceDto.getName().trim());
        }

        String query = queryBuilder.toString().trim();

        GooglePlaceResponseDTO response = googlePlaceClient.getLocation(query);

        // 첫 검색 실패 시, 장소 주소로 재검색 시도
        if ((response == null || response.getResults() == null || response.getResults().isEmpty())
                && updatedPlaceDto.getAddress() != null && !updatedPlaceDto.getAddress().isBlank()) {
            response = googlePlaceClient.getLocation(updatedPlaceDto.getAddress().trim());
        }

        // 둘 다 실패하면 예외 발생
        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            throw new InvalidGooglePlaceException(ErrorStatus.INVALID_GOOGLE_PLACE);
        }

        GooglePlaceResponseDTO.Result location = response.getResults().get(0);
        if (location != null) {
            place.setLatitude(location.getGeometry().getLocation().getLat());
            place.setLongitude(location.getGeometry().getLocation().getLng());
            place.setAddress(location.getFormatted_address());
        }
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

    @Transactional(readOnly = true)
    public TravelRoom findRoomEntityById(Long roomId) {
        return roomDAO.findById(roomId)
                .orElseThrow(() -> new RoomHandler(ErrorStatus.PLAN_NOT_FOUND));
    }
}
