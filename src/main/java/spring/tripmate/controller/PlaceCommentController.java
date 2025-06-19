package spring.tripmate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import spring.tripmate.domain.apiPayload.ApiResponse;
import spring.tripmate.dto.PlaceCommentRequestDTO;
import spring.tripmate.dto.PlaceCommentResponseDTO;
import spring.tripmate.service.PlaceCommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class PlaceCommentController {

    private final PlaceCommentService commentService;

    // 댓글 등록
    @PostMapping
    public ApiResponse<String> addComment(@RequestHeader("Authorization") String authHeader,
                                          @RequestBody PlaceCommentRequestDTO request) {
        String token = authHeader.replace("Bearer ", "").trim();
        commentService.addComment(token, request);
        return ApiResponse.onSuccess("댓글이 등록되었습니다.");
    }

    // 댓글 조회
    @GetMapping
    public ApiResponse<List<PlaceCommentResponseDTO>> getComments(@RequestParam Long placeId) {
        List<PlaceCommentResponseDTO> comments = commentService.getComments(placeId);
        return ApiResponse.onSuccess(comments);
    }
}
