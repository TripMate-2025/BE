package spring.tripmate.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.context.annotation.Lazy;

import spring.tripmate.domain.Consumer;
import spring.tripmate.security.JwtProvider;
import spring.tripmate.service.ConsumerService;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final ConsumerService consumerService;

    public OAuth2LoginSuccessHandler(JwtProvider jwtProvider, @Lazy ConsumerService consumerService) {
        this.jwtProvider = jwtProvider;
        this.consumerService = consumerService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        System.out.println("성공 핸들러 진입!");

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        if (email == null) {
            throw new RuntimeException("로그인 실패: 이메일 정보가 없습니다.");
        }

        String jwt = jwtProvider.createToken(email, "USER");

        Consumer consumer = consumerService.findByEmail(email);
        boolean nicknameSet = Boolean.TRUE.equals(consumer.getNicknameSet());

        String redirectUrl = UriComponentsBuilder
                .fromUriString(nicknameSet ? "http://localhost:3000" : "http://localhost:3000/nickname-setting")
                .queryParam("token", jwt)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
