package spring.tripmate.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("[JwtAuthenticationFilter] Request URI: " + request.getRequestURI());

        String token = jwtProvider.resolveToken(request);
        System.out.println("[JwtAuthenticationFilter] Token extracted: " + token);

        if (token != null && jwtProvider.validateToken(token)) {
            Authentication authentication = jwtProvider.getAuthentication(token);
            System.out.println("[JwtAuthenticationFilter] Authentication set: " + authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            System.out.println("[JwtAuthenticationFilter] No valid token found");
        }

        filterChain.doFilter(request, response);
    }
}
