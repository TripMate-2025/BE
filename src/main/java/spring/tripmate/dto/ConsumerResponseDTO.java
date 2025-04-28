package spring.tripmate.dto;

import lombok.*;

public class ConsumerResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterDTO {
        private Long id;
        private String username;
        private String email;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginDTO {
        private Long id;
        private String username;
        private String email;
        private String token; 
    }
}
