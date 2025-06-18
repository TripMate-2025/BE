package spring.tripmate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import spring.tripmate.client.GeminiClient;
import spring.tripmate.client.GooglePlaceClient;
import spring.tripmate.converter.TravelRoomConverter;
import spring.tripmate.dao.*;
import spring.tripmate.domain.Consumer;
import spring.tripmate.domain.TravelPlace;
import spring.tripmate.domain.TravelPlan;
import spring.tripmate.domain.TravelRoom;
import spring.tripmate.domain.apiPayload.code.status.ErrorStatus;
import spring.tripmate.domain.apiPayload.exception.handler.*;
import spring.tripmate.domain.enums.CategoryType;
import spring.tripmate.domain.mapping.RoomMember;
import spring.tripmate.domain.mapping.RoomMemberId;
import spring.tripmate.dto.*;
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
    private final GeminiClient geminiClient;
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
    public Void updateRoom(Long roomId, TravelRoomRequestDTO.UpdateDTO request, String authHeader) {
        System.out.println("[DEBUG] updatePlaceInRoom 시작 - roomId: " + roomId);

        Consumer requester = getConsumerFromHeader(authHeader);

        TravelRoom room = roomDAO.findByIdWithPlanWithPlaces(roomId)
                .orElseThrow(() -> new PlanHandler(ErrorStatus.ROOM_NOT_FOUND));

        boolean isMember = room.getMembers().stream()
                .anyMatch(m -> m.getMember().getId().equals(requester.getId()));

        if (!isMember) throw new PlanHandler(ErrorStatus.PLAN_NOT_FOUND);

        TravelPlan plan = room.getPlan();
        if (plan == null) throw new PlanHandler(ErrorStatus.PLAN_NOT_FOUND);

        if (request.getName() != null && !request.getName().isEmpty()) {
            room.setName(request.getName());
        }
        if (request.getPlace() != null) {
            Long placeId = request.getPlace().getPlaceId();
            TravelPlace place = plan.getPlaces().stream()
                    .filter(p -> p.getId().equals(placeId))
                    .findFirst()
                    .orElseThrow(() -> new PlanHandler(ErrorStatus.PLACE_NOT_FOUND));

            if (request.getPlace().getName() != null && !request.getPlace().getName().isEmpty()) {
                GooglePlaceResponseDTO response = googlePlaceClient.getLocation(request.getCountry() + request.getCity() + " " + request.getPlace().getName());
                System.out.println(response);
                if (response == null || response.getResults() == null || response.getResults().isEmpty()){
                    throw new InvalidGooglePlaceException(ErrorStatus.INVALID_GOOGLE_PLACE);
                }

                GooglePlaceResponseDTO.Result location = response.getResults().get(0);
                if (location != null) {
                    place.setName(request.getPlace().getName());
                    place.setLatitude(location.getGeometry().getLocation().getLat());
                    place.setLongitude(location.getGeometry().getLocation().getLng());
                    place.setAddress(location.getFormatted_address());
                }else{
                    throw new PlanHandler(ErrorStatus.PLACE_NOT_FOUND);
                }
            }
            if (request.getPlace().getTime() != null){
                place.setTime(request.getPlace().getTime());
            }
            if (request.getPlace().getDescription() != null && !request.getPlace().getDescription().isEmpty()) {
                place.setDescription(request.getPlace().getDescription());
            }

            String locationCategory = "장소 이름: " + place.getName() + ", 장소 주소: " + place.getAddress() + ", 장소 설명: " + place.getDescription();
            String prompt = buildPrompt(locationCategory);
            String result;

            try {
                result = geminiClient.requestGemini(prompt);
                result = result.trim();
            } catch (RestClientException e) {
                e.printStackTrace();
                throw new GeminiCallFailedException(ErrorStatus.GEMINI_API_CALL_FAILED);
            }
            CategoryType newCategory = CategoryType.valueOf(result);
            place.setCategory(newCategory);
        }

        planDAO.save(plan);

        System.out.println("[DEBUG] room 수정 및 저장 완료 - roomId: " + roomId);
        return null;
    }

    private String buildPrompt(String location) {
        StringBuilder sb = new StringBuilder();

        sb.append("제공된 장소와 설명을 보고 장소 유형(category)을 지정해줘. ");
        sb.append(location);

        sb.append("""
                  - category (string): 장소 유형. 다음 중 하나여야 해(enum 타입이므로 다음 {}안의 타입만 값으로 올 수 있음. 특히 LOCAL_FESTIVAL은 category의 enum이 아니라 style의 enum임.):{
                    - SIGHTSEEING ("관광명소")
                    - FOOD ("음식")
                    - CAFE ("카페")
                    - SHOPPING ("쇼핑")
                    - NATURE ("자연")
                    - CULTURE ("문화")
                    - ACTIVITY ("체험")
                    - RELAX ("휴식")
                    - NIGHT ("야경/밤")
                    - OTHER ("기타")
                  }
                """);

        return sb.toString();
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
        System.out.println("[DEBUG] addMember 시작 - roomId: " + roomId);

        Consumer consumer = getConsumerFromHeader(authHeader);
        System.out.println("[DEBUG] 인증된 사용자: " + consumer.getEmail());

        TravelRoom room = roomDAO.findById(roomId)
                .orElseThrow(() -> {
                    System.out.println("[ERROR] 해당 roomId를 찾을 수 없음: " + roomId);
                    return new RoomHandler(ErrorStatus.PLAN_NOT_FOUND);
                });

        // 이미 등록된 멤버인지 확인
        RoomMemberId memberId = new RoomMemberId(consumer.getId(), room.getId());
        if (memberDAO.findById(memberId).isPresent()) {
            System.out.println("[ERROR] 이미 방에 참가된 사용자: " + consumer.getId());
            throw new RoomHandler(ErrorStatus.ALREADY_ROOM_MEMBER);
        }

        RoomMember member = RoomMember.builder()
                .id(memberId)
                .member(consumer)
                .room(room)
                .build();

        memberDAO.save(member);

        System.out.println("[DEBUG] 멤버 저장 완료: " + consumer.getEmail());

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
}
