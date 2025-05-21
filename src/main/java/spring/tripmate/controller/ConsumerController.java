package spring.tripmate.controller;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ConsumerResponseDTO.RegisterDTO> register(
            @RequestBody @Valid ConsumerRequestDTO.RegisterDTO request
    ) {
        ConsumerResponseDTO.RegisterDTO response = consumerService.register(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<ConsumerResponseDTO.LoginDTO> login(
            @RequestBody @Valid ConsumerRequestDTO.LoginDTO request
    ) {
        ConsumerResponseDTO.LoginDTO response = consumerService.login(request);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/check-nickname")
    public ResponseEntity<?> checkNickname(@RequestParam("nickname") String nickname) {
        boolean exists = consumerService.existsByNickname(nickname);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam("email") String email) {
        boolean exists = consumerService.existsByEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/me")
    public ResponseEntity<ConsumerResponseDTO.LoginDTO> getCurrentUser(
            @RequestHeader("Authorization") String authHeader
    ) {
        // 1. 토큰 파싱 (Bearer 제거)
        String token = authHeader.replace("Bearer ", "");
        String email = jwtProvider.getEmailFromToken(token);

        // 2. 이메일로 사용자 조회
        Consumer consumer = consumerService.findByEmail(email);

        // 3. DTO로 변환
        ConsumerResponseDTO.LoginDTO response = ConsumerResponseDTO.LoginDTO.builder()
                .id(consumer.getId())
                .nickname(consumer.getNickname())
                .name(consumer.getName())
                .email(consumer.getEmail())
                .token(token)
                .nicknameSet(consumer.getNicknameSet())
                .profile(consumer.getProfile())
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateMe(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("nickname") String nickname,
            @RequestPart("email") String email,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        String token = authHeader.replace("Bearer ", "");
        String emailFromToken = jwtProvider.getEmailFromToken(token);

        try {
            consumerService.updateConsumer(emailFromToken, nickname, email, profileImage);
            return ResponseEntity.ok(Map.of("message", "사용자 정보가 성공적으로 수정되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PatchMapping("/nickname")
    public ResponseEntity<?> updateNickname(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request
    ) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtProvider.getEmailFromToken(token);
        String newNickname = request.get("nickname");

        try {
            consumerService.updateNickname(email, newNickname);
            return ResponseEntity.ok(Map.of("message", "닉네임이 성공적으로 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/{consumerId}/posts")
    public ApiResponse<PostResponseDTO.SummaryDTO> getPostsByWriter(@PathVariable("consumerId") Long writerId,
                                                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                                                    @RequestParam(name = "size", defaultValue = "15") int size){
        PostResponseDTO.SummaryDTO response = consumerService.getPostsByWriter(writerId, page, size);
        return ApiResponse.onSuccess(response);
    }
}
