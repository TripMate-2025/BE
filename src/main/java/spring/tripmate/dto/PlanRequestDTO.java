package spring.tripmate.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import spring.tripmate.domain.enums.CategoryType;
import spring.tripmate.domain.enums.CompanionType;
import spring.tripmate.domain.enums.StyleType;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class PlanRequestDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDTO{
        @NotBlank
        private String country;
        @NotBlank
        private String city;
        @NotNull
        private CompanionType companion;

        private List<StyleType> style;
        @NotNull
        private Date startDate;
        @NotNull
        private Date endDate;

        private String customizing;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDTO{
        private String country;

        private String city;

        private CompanionType companion;

        private List<StyleType> style;

        private Date startDate;

        private Date endDate;

        private List<PlaceDTO> places;

        @Getter
        @Setter
        public static class PlaceDTO {
            @NotNull
            private Long placeId;

            private String name;

            private CategoryType category;

            private String description;

            private LocalDateTime time;

            private String address;

            private Double latitude;

            private Double longitude;
        }
    }
}
