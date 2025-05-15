package spring.tripmate.converter;

import spring.tripmate.domain.TravelPlace;
import spring.tripmate.domain.TravelPlan;
import spring.tripmate.dto.PlanResponseDTO;

public class TravelPlaceConverter {
    public static TravelPlace toPlace(TravelPlan plan, PlanResponseDTO.CreatePlanDTO.CreatePlaceDTO place) {
        return TravelPlace.builder()
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
}
