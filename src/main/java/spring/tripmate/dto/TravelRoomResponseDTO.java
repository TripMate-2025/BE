package spring.tripmate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import spring.tripmate.domain.PlaceComment;
import spring.tripmate.domain.enums.CategoryType;
import spring.tripmate.domain.enums.CompanionType;
import spring.tripmate.domain.enums.StyleType;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class TravelRoomResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomDTO {
        private Long roomId;
        private String name;
        private PlanDTO plan;
        private ConsumerResponseDTO.RoomMembersDTO members;

        @Builder
        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PlanDTO{
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

                private List<PlaceComment> comments;
            }
        }
    }
}