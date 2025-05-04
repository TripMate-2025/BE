package spring.tripmate.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import spring.tripmate.dao.ConsumerDAO;
import spring.tripmate.domain.Consumer;
import spring.tripmate.domain.enums.ProviderType;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final ConsumerDAO consumerDAO;
    private final JwtProvider jwtProvider;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = new DefaultOAuth2UserService().loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google" or "naver"
        Map<String, Object> originalAttributes = oauthUser.getAttributes();

        String email;
        String name;

        if ("naver".equals(provider)) {
            // 네이버는 사용자 정보가 response 키 내부에 있음
            Map<String, Object> response = (Map<String, Object>) originalAttributes.get("response");
            email = (String) response.get("email");
            name = (String) response.get("name");
        } else if ("kakao".equals(provider)) {
            // 이메일 없이 카카오 고유 ID 기반 식별자 생성
            String kakaoId = String.valueOf(originalAttributes.get("id"));
            Map<String, Object> kakaoAccount = (Map<String, Object>) originalAttributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            email = kakaoId + "@kakao.com"; // 가짜 이메일
            name = (String) profile.get("nickname");
        } else {
            // 구글, 기본
            email = oauthUser.getAttribute("email");
            name = oauthUser.getAttribute("name");
        }
        
        Consumer consumer = Optional.ofNullable(consumerDAO.findByEmail(email))
                .orElseGet(() -> {
                    Consumer newUser = Consumer.builder()
                            .email(email)
                            .name(name)
                            .nickname("google_" + System.currentTimeMillis())
                            .password("SOCIAL") 
                            .provider(ProviderType.valueOf(provider.toUpperCase()))
                            .build();
                    return consumerDAO.save(newUser);
                });

        String jwt = jwtProvider.createToken(email, "ROLE_USER");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", email);
        attributes.put("name", name);
        attributes.put("jwt", jwt);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email"
        );
    }
}
