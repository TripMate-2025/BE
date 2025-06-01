package spring.tripmate.controller;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import spring.tripmate.domain.Consumer;
import spring.tripmate.domain.apiPayload.ApiResponse;
import spring.tripmate.dto.ConsumerRequestDTO;
import spring.tripmate.dto.ConsumerResponseDTO;
import spring.tripmate.dto.PostResponseDTO;
import spring.tripmate.security.JwtProvider;
import spring.tripmate.service.ConsumerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/consumers")
@RequiredArgsConstructor
public class ConsumerController {

    private final ConsumerService consumerService;
    private final JwtProvider jwtProvider;

    @PostMapping("/register")
    public ApiResponse<ConsumerResponseDTO.RegisterDTO> register(
            @RequestBody @Valid ConsumerRequestDTO.RegisterDTO request
    ) {
        ConsumerResponseDTO.RegisterDTO response = consumerService.register(request);
        return ApiResponse.onSuccess(response);
    }

    @PostMapping("/login")
    public ApiResponse<ConsumerResponseDTO.LoginDTO> login(
            @RequestBody @Valid ConsumerRequestDTO.LoginDTO request
    ) {
        ConsumerResponseDTO.LoginDTO response = consumerService.login(request);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/check-nickname")
    public ApiResponse<Map<String, Boolean>> checkNickname(@RequestParam("nickname") String nickname) {
        boolean exists = consumerService.existsByNickname(nickname);
        return ApiResponse.onSuccess(Map.of("exists", exists));
    }

    @GetMapping("/check-email")
    public ApiResponse<Map<String, Boolean>> checkEmail(@RequestParam("email") String email) {
        boolean exists = consumerService.existsByEmail(email);
        return ApiResponse.onSuccess(Map.of("exists", exists));
    }

    @GetMapping("/me")
    public ApiResponse<ConsumerResponseDTO.LoginDTO> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ApiResponse.onFailure("401", "유효하지 않은 인증 헤더입니다.", null);
        }

        String token = authHeader.replace("Bearer ", "").trim();
        if (token.isEmpty()) {
            return ApiResponse.onFailure("401", "토큰이 비어있습니다.", null);
        }

        try {
            String email = jwtProvider.getEmailFromToken(token);
            Consumer consumer = consumerService.findByEmail(email);

            ConsumerResponseDTO.LoginDTO response = ConsumerResponseDTO.LoginDTO.builder()
                    .id(consumer.getId())
                    .nickname(consumer.getNickname())
                    .name(consumer.getName())
                    .email(consumer.getEmail())
                    .token(token)
                    .nicknameSet(consumer.getNicknameSet())
                    .profile(consumer.getProfile())
                    .build();

            return ApiResponse.onSuccess(response);
        } catch (Exception e) {

            return ApiResponse.onFailure("401", "유효하지 않은 토큰입니다.", null);
        }
    }


    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, String>> updateMe(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("nickname") String nickname,
            @RequestPart("email") String email,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        String token = authHeader.replace("Bearer ", "");
        String emailFromToken = jwtProvider.getEmailFromToken(token);

        try {
            consumerService.updateConsumer(emailFromToken, nickname, email, profileImage);
            return ApiResponse.onSuccess(Map.of("message", "사용자 정보가 성공적으로 수정되었습니다."));
        } catch (IllegalArgumentException e) {
            return ApiResponse.onFailure("400", e.getMessage(), null);
        }
    }

    @PatchMapping("/nickname")
    public ApiResponse<Map<String, String>> updateNickname(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request
    ) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtProvider.getEmailFromToken(token);
        String newNickname = request.get("nickname");

        try {
            consumerService.updateNickname(email, newNickname);
            return ApiResponse.onSuccess(Map.of("message", "닉네임이 성공적으로 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ApiResponse.onFailure("400", e.getMessage(), null);
        }
    }

    @GetMapping("/{consumerId}/posts")
    public ApiResponse<PostResponseDTO.SummaryDTO> getPostsByWriter(@PathVariable("consumerId") Long writerId,
                                                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                                                    @RequestParam(name = "size", defaultValue = "15") int size) {
        PostResponseDTO.SummaryDTO response = consumerService.getPostsByWriter(writerId, page, size);
        return ApiResponse.onSuccess(response);
    }
}
