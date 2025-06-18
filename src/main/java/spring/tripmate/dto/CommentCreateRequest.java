package spring.tripmate.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class CommentCreateRequest {
    @NotNull
    private Long consumerId;

    @NotBlank
    private String content;
}

