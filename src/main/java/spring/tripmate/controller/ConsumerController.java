package spring.tripmate.controller;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring.tripmate.dto.ConsumerRequestDTO;
import spring.tripmate.dto.ConsumerResponseDTO;
import spring.tripmate.service.ConsumerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/consumers")
@RequiredArgsConstructor
public class ConsumerController {

    private final ConsumerService consumerService;

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

}
