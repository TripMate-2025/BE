package spring.tripmate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class PostRequestDTO {
    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDTO{
        @NotBlank
        private String title;

        private List<MultipartFile> images;
        @NotBlank
        private String content;
        @NotNull
        private Long planId;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDTO{
        private String title;
        private String content;
        private Long planId;

        private List<Long> deleteImageIds;

        private List<MultipartFile> newImages;
    }
}
