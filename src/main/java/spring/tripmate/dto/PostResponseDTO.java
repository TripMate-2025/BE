package spring.tripmate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class PostResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDTO{
        @NotNull
        private Long postId;

        private LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDTO{
        private Map<String, Object> updatedFields;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryDTO{
        private List<SummaryPostDTO> posts;

        @Getter
        @Setter
        public static class SummaryPostDTO{
            @NotNull
            private Long postId;
            @NotBlank
            private String nickname;

            private String profile;
            @NotBlank
            private String title;

            private List<String> images;
            @NotBlank
            private String content;
        }
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailDTO{
        @NotNull
        private Long postId;
        @NotBlank
        private String nickname;

        private String profile;
        @NotBlank
        private String title;

        private List<String> images;
        @NotBlank
        private String content;
        @NotNull
        private List<PlanResponseDTO.PlanDTO> plan;
    }
}
