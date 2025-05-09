package team.klover.server.global.security.filter;


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
import team.klover.server.global.redis.RedisService;
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
        String method = request.getMethod();

        // Skip filtering for these paths
        return path.startsWith("/h2-console")
                || path.startsWith("/api/v1/auth/signup")
                || path.startsWith("/api/v1/auth/login")
                || path.startsWith("/oauth2/")
                || path.startsWith("/api/v1/auth/google")
                || path.startsWith("/api/v1/auth/line")
                || path.startsWith("/api/v1/auth/refresh")
                || path.startsWith("/v1/api-docs")
                || path.startsWith("/v1/api-docs/swagger-config")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/api/v1/translate")
                || (path.startsWith("/api/v1/notification"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = jwtTokenProvider.getJwtFromHeader(request);

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 인증이 필요 없는 URL 리스트
        boolean isPublicApi = (path.startsWith("/api/v1/comm-post") && method.equals("GET")) || (path.startsWith("/api/v1/tour-post") && method.equals("GET"));
        if (!isPublicApi && !StringUtils.hasText(token)) {
            log.warn("JWT 토큰이 없습니다.");

            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"message\": \"인증 실패\"}");
            return;
        }

        boolean decodingSuccess = false;
        try{
            jwtTokenProvider.decodeToken(token);
            decodingSuccess = true;
        } catch (Exception jwtException){
            log.warn("유효하지 않은 Access Token 토큰입니다.");

            if(!isPublicApi) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.getWriter().write("{\"message\": \"유효하지 않은 Access Token 입니다.\"}");
                return;
            }
        }

        String email = "";
        if(decodingSuccess) {
            email = jwtTokenProvider.getMemberEmailFromToken(token);
            log.info("정상적으로 사용자 정보를 토큰으로부터 가져왔습니다. Email: {}", email);
        }

        try {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("SecurityContext 에 인증 정보 설정 완료: {}", authentication);
        } catch (Exception e) {
            if(!isPublicApi) {
                log.error("인증 처리 실패: {}", e.getMessage());

                SecurityContextHolder.clearContext();
                redisService.deleteRefreshToken(email);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.getWriter().write("{\"message\": \"인증 실패\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

}
