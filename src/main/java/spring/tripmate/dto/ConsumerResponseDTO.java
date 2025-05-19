package spring.tripmate.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.*;

public class ConsumerResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterDTO {
        private Long id;
        private String nickname;
        private String name;
        private String email;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginDTO {
        private Long id;
        private String nickname;
        private String name;
        private String email;
        private String token; 
        private Boolean nicknameSet;
        private String profile;
    }
    
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomMembersDTO {
        private List<MemberDTO> members;

        @Getter
        @Setter
        public static class MemberDTO {
            @NotNull
            private Long memberId;
            @NotNull
            private String email;
            
            private String profile;
            @NotNull
            private String username;
        }
    }
}
