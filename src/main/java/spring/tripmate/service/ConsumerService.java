package spring.tripmate.service;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import spring.tripmate.dao.ConsumerDAO;
import spring.tripmate.domain.Consumer;
import spring.tripmate.domain.enums.ProviderType;
import spring.tripmate.dto.ConsumerRequestDTO;
import spring.tripmate.dto.ConsumerResponseDTO;
import spring.tripmate.security.JwtProvider;

@Service
@RequiredArgsConstructor
public class ConsumerService {

    private final ConsumerDAO consumerDAO;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public ConsumerResponseDTO.RegisterDTO register(ConsumerRequestDTO.RegisterDTO request) {
        if (consumerDAO.findByEmail(request.getEmail()) != null) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        Consumer consumer = Consumer.builder()
                .nickname(request.getNickname())
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(ProviderType.LOCAL)
                .nicknameSet(true)
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

        String token = jwtProvider.createToken(consumer.getEmail(), "USER");

        return ConsumerResponseDTO.LoginDTO.builder()
                .id(consumer.getId())
                .nickname(consumer.getNickname())
                .name(consumer.getName())
                .email(consumer.getEmail())
                .token(token)
                .nicknameSet(consumer.getNicknameSet())
                .build();
    }


    public boolean existsByNickname(String nickname) {
        return consumerDAO.existsByNickname(nickname);
    }

    public boolean existsByEmail(String email) {
        return consumerDAO.existsByEmail(email);
    }

    public Consumer findByEmail(String email) {
        return Optional.ofNullable(consumerDAO.findByEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public void updateNickname(String email, String newNickname) {
        // 중복 검사
        if (existsByNickname(newNickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        Consumer consumer = findByEmail(email);
        consumer.setNickname(newNickname);
        consumer.setNicknameSet(true);
    }

}
