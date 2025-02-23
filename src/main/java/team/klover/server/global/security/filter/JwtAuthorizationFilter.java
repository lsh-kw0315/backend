package team.klover.server.global.security.filter;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import team.klover.server.domain.member.v1.dto.MemberDto;
import team.klover.server.global.redis.RedisService;
import team.klover.server.global.security.custom.CustomUserDetails;
import team.klover.server.global.security.custom.CustomUserDetailsService;
import team.klover.server.global.security.provider.JwtTokenProvider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// JWT 를 검증하고, 인증된 사용자의 정보를 SecurityContext 에 저장하는 역할
@Slf4j(topic = "JwtAuthorizationFilter")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final RedisService redisService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Skip filtering for these paths
        return path.startsWith("/h2-console")
                || path.startsWith("/api/v1/members/signup")
                || path.startsWith("/api/v1/members/login")
                || path.startsWith("/oauth2/")
                || path.startsWith("/api/v1/members/auth")
                || path.startsWith("/api/v1/members/google")
                || path.startsWith("/api/v1/members/line")
                || path.startsWith("/api/v1/members/logout")
                || path.startsWith("/api/v1/members/refresh");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = jwtTokenProvider.getJwtFromHeader(request);

        if (!StringUtils.hasText(token)) {
            log.warn("JWT 토큰이 없습니다.");

            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"message\": \"인증 실패\"}");
            return;
        }

        try{
            jwtTokenProvider.decodeToken(token);
        } catch (JwtException jwtException){
            log.warn("유효하지 않은 Access Token 토큰입니다.");

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"message\": \"유효하지 않은 Access Token 입니다.\"}");
            return;
        }


        String email = jwtTokenProvider.getMemberEmailFromToken(token);
        log.info("정상적으로 사용자 정보를 토큰으로부터 가져왔습니다. Email: {}", email);

        try {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("SecurityContext 에 인증 정보 설정 완료: {}", authentication);
        } catch (Exception e) {
            log.error("인증 처리 실패: {}", e.getMessage());

            SecurityContextHolder.clearContext();
            redisService.deleteRefreshToken(email);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"message\": \"인증 실패\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

}
