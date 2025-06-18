package spring.tripmate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

public class TravelRoomRequestDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDTO{
        private String name;

        private String country;

        private String city;

        private Date startDate;

        private Date endDate;

        private PlaceDTO place;

        @Getter
        @Setter
        public static class PlaceDTO {
            @NotNull
            private Long placeId;

            private String name;

            private String description;

            private LocalDateTime time;
        }
    }
}
