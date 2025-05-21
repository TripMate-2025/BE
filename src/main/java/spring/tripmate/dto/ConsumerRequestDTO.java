package spring.tripmate.dto;

import jakarta.validation.constraints.*;
import lombok.*;

public class ConsumerRequestDTO {
	@Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterDTO {
        @NotBlank
        private String nickname;
        @NotBlank
        private String name;
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginDTO {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDTO {
        private String nickname;
        @Email
        private String email;
    }
}
