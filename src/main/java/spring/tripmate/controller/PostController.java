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

    @GetMapping("/likes")
    public ApiResponse<PostResponseDTO.SummaryDTO> getLikedPosts(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "15") int size) {
        PostResponseDTO.SummaryDTO response = postService.getLikedPosts(authHeader, page, size);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/mine")
    public ApiResponse<PostResponseDTO.SummaryDTO> getMyPosts(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "15") int size) {

        PostResponseDTO.SummaryDTO response = postService.getMyPosts(authHeader, page, size);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping
    public ApiResponse<PostResponseDTO.SummaryDTO> getPostsByCountry(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                                     @RequestParam(value = "country", required = false) String country,
                                                                     @RequestParam(name = "page", defaultValue = "0") int page,
                                                                     @RequestParam(name = "size", defaultValue = "15") int size){
        PostResponseDTO.SummaryDTO response = postService.getPostsByCountry(authHeader, country, page, size);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostResponseDTO.DetailDTO> getPostById(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                              @PathVariable("postId") Long postId){
        PostResponseDTO.DetailDTO response = postService.getPostById(authHeader, postId);
        return ApiResponse.onSuccess(response);
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable("postId") Long postId){
        postService.deletePost(postId);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/{postId}/like")
    public ApiResponse<Void> likePost(@RequestHeader("Authorization") String authHeader,
                                      @PathVariable("postId") Long postId){
        postService.likePost(authHeader, postId);
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/{postId}/like")
    public ApiResponse<Void> unlikePost(@RequestHeader("Authorization") String authHeader,
                                        @PathVariable("postId") Long postId){
        postService.unlikePost(authHeader, postId);
        return ApiResponse.onSuccess(null);
    }

}
