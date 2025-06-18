package spring.tripmate.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceCommentDTO {
    private Long id;
    private String content;
    private String nickname;
    private String profileImg;
    private LocalDateTime createdDate;
}
