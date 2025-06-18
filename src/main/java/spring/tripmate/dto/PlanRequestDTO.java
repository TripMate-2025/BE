package spring.tripmate.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import spring.tripmate.domain.enums.CompanionType;
import spring.tripmate.domain.enums.StyleType;

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
}
