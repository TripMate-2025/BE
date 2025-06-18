package spring.tripmate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import spring.tripmate.domain.enums.CategoryType;
import spring.tripmate.domain.enums.CompanionType;
import spring.tripmate.domain.enums.StyleType;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PlanResponseDTO {
    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePlanDTO{
        @NotNull
        private UUID planId; //redis 저장을 위한 임시 ID
        @NotNull
        private String title;
        @NotNull
        private Date startDate;
        @NotNull
        private Date endDate;
        @NotBlank
        private String country;
        @NotBlank
        private String city;
        @NotNull
        private CompanionType companion;

        private List<StyleType> style;
        @NotNull
        private List<CreatePlaceDTO> places;

        @Getter
        @Setter
        public static class CreatePlaceDTO {
            @NotNull
            private Long id;
            @NotBlank
            private String name;
            @NotNull
            private CategoryType category;
            @NotNull
            private String description;
            @NotNull
            private LocalDateTime time;

            private String address;
            @NotNull
            private Double latitude;
            @NotNull
            private Double longitude;
            @NotNull
            private Integer dayNumber;
        }
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanDTO{
        @NotNull
        private Long planId;
        @NotNull
        private String title;
        @NotNull
        private Date startDate;
        @NotNull
        private Date endDate;
        @NotBlank
        private String country;
        @NotBlank
        private String city;
        @NotNull
        private CompanionType companion;

        private List<StyleType> style;
        @NotNull
        private List<PlaceDTO> places;

        @Builder
        @Getter
        @Setter
        public static class PlaceDTO {
            @NotNull
            private Long placeId;
            @NotBlank
            private String name;
            @NotNull
            private CategoryType category;
            @NotNull
            private String description;
            @NotNull
            private LocalDateTime time;

            private String address;
            @NotNull
            private Double latitude;
            @NotNull
            private Double longitude;
            @NotNull
            private Integer dayNumber;
        }
    }
}
