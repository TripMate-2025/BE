package spring.tripmate.converter;

import spring.tripmate.domain.TravelRoom;
import spring.tripmate.domain.Consumer;
import spring.tripmate.dto.ConsumerResponseDTO.RoomMembersDTO;
import spring.tripmate.dto.ConsumerResponseDTO.RoomMembersDTO.MemberDTO;
import spring.tripmate.dto.TravelRoomResponseDTO;
import spring.tripmate.domain.enums.CompanionType;
import spring.tripmate.domain.enums.StyleType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TravelRoomConverter {

    public static TravelRoomResponseDTO.RoomDTO toRoomDTO(TravelRoom room, List<Consumer> members) {
        var plan = room.getPlan();

        // destination 분리
        String destination = plan.getDestination() != null ? plan.getDestination() : "";
        String[] parts = destination.split(" ", 2);
        String country = parts.length > 0 ? parts[0] : "";
        String city = parts.length > 1 ? parts[1] : "";

        // theme 분리 및 companion, style 파싱
        String theme = plan.getTheme() != null ? plan.getTheme() : "ALONE/";
        String[] themeParts = theme.split("/", 2);

        CompanionType companion = CompanionType.valueOf(themeParts[0]);

        List<StyleType> style = List.of();
        if (themeParts.length > 1 && !themeParts[1].isEmpty()) {
            style = Arrays.stream(themeParts[1].split(";"))
                    .map(String::trim)
                    .map(StyleType::valueOf)
                    .collect(Collectors.toList());
        }

        // place DTO 리스트 생성
        List<TravelRoomResponseDTO.RoomDTO.PlanDTO.PlaceDTO> placeDTOs = plan.getPlaces().stream()
                .map(p -> {
                    var dto = new TravelRoomResponseDTO.RoomDTO.PlanDTO.PlaceDTO();
                    dto.setName(p.getName());
                    dto.setCategory(p.getCategory());
                    dto.setDescription(p.getDescription());
                    dto.setTime(p.getTime());
                    dto.setAddress(p.getAddress());
                    dto.setLatitude(p.getLatitude());
                    dto.setLongitude(p.getLongitude());
                    dto.setDayNumber(p.getDayNumber());
                    dto.setComments(p.getComments());
                    return dto;
                })
                .collect(Collectors.toList());

        // plan DTO 생성
        var planDTO = TravelRoomResponseDTO.RoomDTO.PlanDTO.builder()
                .planId(plan.getId())
                .title(plan.getTitle())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .country(country)
                .city(city)
                .companion(companion)
                .style(style)
                .places(placeDTOs)
                .build();

        // 멤버 DTO 리스트 생성
        List<MemberDTO> memberDTOs = members.stream()
                .map(c -> {
                    MemberDTO m = new MemberDTO();
                    m.setMemberId(c.getId());
                    m.setEmail(c.getEmail());
                    m.setProfile(c.getProfile());
                    m.setUsername(c.getNickname());
                    return m;
                })
                .collect(Collectors.toList());

        RoomMembersDTO membersDTO = RoomMembersDTO.builder()
                .members(memberDTOs)
                .build();

        // 최종 RoomDTO 반환
        return TravelRoomResponseDTO.RoomDTO.builder()
                .roomId(room.getId())
                .name(room.getName())
                .plan(planDTO)
                .members(membersDTO)
                .build();
    }
}
