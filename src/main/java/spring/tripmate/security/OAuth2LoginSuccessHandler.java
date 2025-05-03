package spring.tripmate.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import spring.tripmate.security.JwtProvider;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

    	System.out.println("✅ 성공 핸들러 진입!");

    	OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        
        // 👉 기본 role은 "USER"로 가정 (필요하면 oAuth2User에서 직접 가져올 수도 있음)
        String role = "USER";

        // ✅ 실제 JWT 생성
        String jwt = jwtProvider.createToken(email, role);

        // ✅ 프론트로 리다이렉트 + token 전달
        String redirectUrl = UriComponentsBuilder
                .fromUriString("http://localhost:3000")
                .queryParam("token", jwt)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
