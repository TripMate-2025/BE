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
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDTO{
        private String title;
        private List<UpdateImageDTO> images;
        private String content;

        @Getter
        @Setter
        public static class UpdateImageDTO {
            @NotNull
            private Long postImageId;
            @NotNull
            private MultipartFile newImage;
        }
    }
}
