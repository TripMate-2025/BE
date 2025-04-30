package spring.tripmate.service;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import spring.tripmate.dao.ConsumerDAO;
import spring.tripmate.domain.Consumer;
import spring.tripmate.dto.ConsumerRequestDTO;
import spring.tripmate.dto.ConsumerResponseDTO;

@Service
@RequiredArgsConstructor
public class ConsumerService {

    private final ConsumerDAO consumerDAO;
    private final PasswordEncoder passwordEncoder;

    public ConsumerResponseDTO.RegisterDTO register(ConsumerRequestDTO.RegisterDTO request) {
        if (consumerDAO.findByEmail(request.getEmail()) != null) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        Consumer consumer = Consumer.builder()
                .nickname(request.getNickname())
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        Consumer savedConsumer = consumerDAO.save(consumer);

        return ConsumerResponseDTO.RegisterDTO.builder()
                .id(savedConsumer.getId())
                .nickname(savedConsumer.getNickname())
                .name(savedConsumer.getName())
                .email(savedConsumer.getEmail())
                .build();
    }
    
    public ConsumerResponseDTO.LoginDTO login(ConsumerRequestDTO.LoginDTO request) {
        // 이메일로 소비자 찾기
        Consumer consumer = Optional.ofNullable(consumerDAO.findByEmail(request.getEmail()))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // 비밀번호 매칭 확인
        if (!passwordEncoder.matches(request.getPassword(), consumer.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 토큰은 아직 시스템에 구현 안 했으니까 임시로 "fake-token" 줌
        String fakeToken = "fake-token"; 

        // DTO로 리턴
        return ConsumerResponseDTO.LoginDTO.builder()
                .id(consumer.getId())
                .nickname(consumer.getNickname())
                .name(consumer.getName())
                .email(consumer.getEmail())
                .token(fakeToken)
                .build();
    }


    public boolean existsByNickname(String nickname) {
        return consumerDAO.existsByNickname(nickname);
    }

    public boolean existsByEmail(String email) {
        return consumerDAO.existsByEmail(email);
    }

}
