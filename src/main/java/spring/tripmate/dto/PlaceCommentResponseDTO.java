package spring.tripmate.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceCommentResponseDTO {
    private Long id;
    private String content;
    private String nickname;
    private String profileImg;
    private String createdAt;
}