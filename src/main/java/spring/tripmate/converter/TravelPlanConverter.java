package spring.tripmate.converter;

import spring.tripmate.domain.Consumer;
import spring.tripmate.domain.TravelPlace;
import spring.tripmate.domain.TravelPlan;
import spring.tripmate.domain.enums.CompanionType;
import spring.tripmate.domain.enums.StyleType;
import spring.tripmate.dto.PlanResponseDTO;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TravelPlanConverter {
    public static TravelPlan toPlan(Consumer consumer, PlanResponseDTO.CreatePlanDTO createPlanDTO) {

        String destination = createPlanDTO.getCountry() + "-" + createPlanDTO.getCity();

        String theme = createPlanDTO.getCompanion().name() + "/" +
                Optional.ofNullable(createPlanDTO.getStyle())
                        .orElse(List.of())
                        .stream()
                        .map(StyleType::name)
                        .collect(Collectors.joining(";"));

        return TravelPlan.builder()
                .title(createPlanDTO.getTitle())
                .startDate(createPlanDTO.getStartDate())
                .endDate(createPlanDTO.getEndDate())
                .destination(destination)
                .theme(theme)
                .consumer(consumer)
                .build();

    }

    public static PlanResponseDTO.PlanDTO toPlanDTO(TravelPlan plan, List<TravelPlace> places){

        String[] parts = plan.getTheme().split("/", 2); // 최대 2개로 분리
        String companionStr = parts[0];
        String[] styleStrs = parts.length > 1 ? parts[1].split(";") : new String[0];

        CompanionType companion = CompanionType.valueOf(companionStr);
        List<StyleType> style = Arrays.stream(styleStrs)
                .map(String::trim)
                .map(StyleType::valueOf)
                .toList();

        String[] destParts = plan.getDestination().split("-", 2);
        String country = destParts[0];
        String city = destParts[1];

        return PlanResponseDTO.PlanDTO.builder()
                .planId(plan.getId())
                .title(plan.getTitle())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .country(country)
                .city(city)
                .companion(companion)
                .style(style)
                .places(places.stream()
                        .map(p -> PlanResponseDTO.PlanDTO.PlaceDTO.builder()
                                .placeId(p.getId())
                                .name(p.getName())
                                .category(p.getCategory())
                                .description(p.getDescription())
                                .time(p.getTime())
                                .address(p.getAddress())
                                .latitude(p.getLatitude())
                                .longitude(p.getLongitude())
                                .dayNumber(p.getDayNumber())
                                .build())
                        .toList())
                .build();
    }
}
