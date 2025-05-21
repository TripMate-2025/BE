package spring.tripmate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import spring.tripmate.domain.apiPayload.ApiResponse;
import spring.tripmate.dto.PostRequestDTO;
import spring.tripmate.dto.PostResponseDTO;
import spring.tripmate.service.PostService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostResponseDTO.CreateDTO> createPost(@RequestHeader("Authorization") String authHeader,
                                                             @Valid @ModelAttribute PostRequestDTO.CreateDTO request){
        PostResponseDTO.CreateDTO response = postService.createPost(authHeader, request);
        return ApiResponse.onSuccess(response);
    }

    @PatchMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostResponseDTO.UpdateDTO> updatePost(@PathVariable("postId") Long postId,
                                                             @Valid @ModelAttribute PostRequestDTO.UpdateDTO request){
        PostResponseDTO.UpdateDTO response = postService.updatePost(postId, request);
        return ApiResponse.onSuccess(response);
    }
}
