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

        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google"
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

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

        Map<String, Object> attributes = new HashMap<>(oauthUser.getAttributes());
        attributes.put("jwt", jwt);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email"
        );
    }
}
