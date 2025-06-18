package spring.tripmate.converter;

import spring.tripmate.domain.TravelPlace;
import spring.tripmate.domain.TravelPlan;
import spring.tripmate.dto.PlanResponseDTO;

public class TravelPlaceConverter {
    public static TravelPlace toPlace(TravelPlan plan, PlanResponseDTO.CreatePlanDTO.CreatePlaceDTO place) {
        return TravelPlace.builder()
                .id(place.getId())
                .name(place.getName())
                .category(place.getCategory())
                .description(place.getDescription())
                .time(place.getTime())
                .address(place.getAddress())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .dayNumber(place.getDayNumber())
                .plan(plan)
                .build();
    }

//    // 엔티티 → DTO 변환
//    public static PlanResponseDTO.PlanDTO.PlaceDTO toPlaceDTO(TravelPlace place) {
//        return PlanResponseDTO.PlanDTO.PlaceDTO.builder()
//                .placeId(place.getId())
//                .name(place.getName())
//                .category(place.getCategory())
//                .description(place.getDescription())
//                .time(place.getTime())
//                .address(place.getAddress())
//                .latitude(place.getLatitude())
//                .longitude(place.getLongitude())
//                .dayNumber(place.getDayNumber())
//                .build();
//    }
}

