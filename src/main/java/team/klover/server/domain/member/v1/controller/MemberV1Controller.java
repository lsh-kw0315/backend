package team.klover.server.domain.member.v1.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import team.klover.server.domain.member.auth.dto.LoginRequestDto;
import team.klover.server.domain.member.auth.dto.SignupRequestDto;
import team.klover.server.domain.member.v1.dto.*;
import team.klover.server.domain.member.v1.entity.Member;
import team.klover.server.domain.member.v1.enums.SocialProvider;
import team.klover.server.domain.member.v1.service.MemberV1Service;
import team.klover.server.global.common.response.ApiResponse;
import team.klover.server.global.redis.RedisService;
import team.klover.server.global.security.custom.CustomUserDetailsService;
import team.klover.server.global.security.provider.JwtTokenProvider;
import team.klover.server.global.util.AuthUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberV1Controller {
    private final MemberV1Service memberService;
    private final BCryptPasswordEncoder passwordEncoder; // 로그인 시 비밀번호 검증에 필요
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;
    private final CustomUserDetailsService customUserDetailsService;

    @Value("${web.google.client-id}")
    private String googleClientId;
    @Value("${android.google.client-id}")
    private String googleAndroidClientId;
    @Value("${android.line.client-id}")
    private String lineAndroidClientId;



    @PostMapping("/signup")
    public ApiResponse<?> signup(@RequestBody SignupRequestDto requestDto) {
        memberService.signup(requestDto);
        return ApiResponse.of("회원 가입 성공");

    }

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody LoginRequestDto requestDto
            //, HttpServletResponse response
    ) {
        Member member = memberService.findLocalMember(requestDto.getEmail());

        if (member == null || !passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new RuntimeException("아이디나 비밀번호가 올바르지 않습니다.");
        }

        MemberDto memberDto = new MemberDto(member);

        String accessToken = jwtTokenProvider.generateAccessToken(memberDto);
        String refreshToken = jwtTokenProvider.generateRefreshToken(member.getEmail());

        /*
        //프론트가 웹인 경우 해금해서 쓰면 됨
        jwtTokenProvider.addJwtToCookie(accessToken, response, "accessToken");
        jwtTokenProvider.addJwtToCookie(refreshToken, response, "refreshToken");
        */

        redisService.saveRefreshToken(member.getEmail(), refreshToken);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(member.getEmail());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return ApiResponse.of(new AuthResponse(accessToken, refreshToken));

    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(HttpServletRequest request,
                                 HttpServletResponse response,
                                 //Authentication authentication,
                                 @RequestBody RefreshRequest refreshRequest) {
        // 1. Spring Security Context Logout 처리
        System.out.println("object:"+refreshRequest);
        Authentication authentication = AuthUtil.getAuthentication();
        if (authentication == null) {
            System.out.println("뭐길래 null:"+authentication);
            return sendErrorResponse(response, "인증 오류.");
        }

        new SecurityContextLogoutHandler().logout(request, response, authentication);

        /*
        // 2. JWT 쿠키 삭제. 웹인 경우 수행
        jwtTokenProvider.deleteJwtFromCookie(response, "accessToken");
        jwtTokenProvider.deleteJwtFromCookie(response, "refreshToken");
        */

        /*
        // 3. RefreshToken 삭제 - 웹
        String refreshToken = jwtTokenProvider.resolveRefreshToken(request);
         */

        //3. RefreshToken 삭제 - 앱
        String refreshToken = refreshRequest.getRefreshToken();
        if (refreshToken == null) {
            return sendErrorResponse(response, "refresh token이 이상합니다.");
        }

        String emailFromToken = jwtTokenProvider.getMemberEmailFromToken(refreshToken);
        String redisToken = redisService.getRefreshToken(emailFromToken);

        System.out.println("꺼내온 이메일:"+emailFromToken);
        System.out.println("redisToken:"+redisToken);
        if(emailFromToken ==null || redisToken == null){
            return sendErrorResponse(response, "refreshToken이 변조되었습니다.");
        }

        redisService.deleteRefreshToken(emailFromToken);

        if(!redisToken.equals(refreshToken)) {
            return sendErrorResponse(response, "redis의 refreshToken과 다릅니다.");
        }


        return ApiResponse.of("로그아웃 성공");
    }

    @GetMapping("/auth")
    public ApiResponse<MemberDto> authorize(HttpServletRequest request,HttpServletResponse response) {
        String accessToken = jwtTokenProvider.getJwtFromHeader(request);
        if (accessToken == null)  return sendErrorResponse(response, "액세스 토큰이 없습니다.");

        if (!jwtTokenProvider.isTokenValid(accessToken))  return sendErrorResponse(response, "액세스 토큰이 유효하지 않습니다.");

        MemberDto member = jwtTokenProvider.getUserInfoFromToken(accessToken);

        return ApiResponse.of(member);
    }

    @PostMapping("/refresh")
    public ApiResponse<?> refresh(HttpServletResponse response,
                                  @RequestBody RefreshRequest refreshRequest) {
        /*
        웹인 경우 쿠키에서 꺼내서 사용
        String refreshToken = jwtTokenProvider.resolveRefreshToken(request);
        */

        //여기서 refreshToken이 동작을 안한다면 로그아웃 처리를 할 수도 있는데 이걸 할지
        //한다면 프론트에서 로그아웃을 요청할지 백엔드에서 로그아웃을 처리할지는 선택
        String refreshToken = refreshRequest.getRefreshToken();
        if (refreshToken == null) {
            return sendErrorResponse(response, "refreshToken이 존재하지 않습니다.");
        }

        if (!jwtTokenProvider.isTokenValid(refreshToken)) {
            return sendErrorResponse(response, "refreshToken이 유효하지 않습니다.");
        }

        String email = jwtTokenProvider.getMemberEmailFromToken(refreshToken);
        String savedRefreshToken = redisService.getRefreshToken(email);

        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            redisService.deleteRefreshToken(email);
            return sendErrorResponse(response, "refreshToken이 변조되었습니다.");
        }

        Member member = memberService.findByEmail(email);

        MemberDto memberDto = new MemberDto(
                member
        );

        String newAccessToken = jwtTokenProvider.generateAccessToken(memberDto);
        return ApiResponse.of(new AuthResponse(newAccessToken, savedRefreshToken));

    }

    @PatchMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> modify(@RequestPart("request") MemberUpdateParam param,
                                      @RequestPart(value = "imageFile", required = false) MultipartFile imageFile, // 이미지 파일 (선택 사항)
                                      HttpServletResponse response
    ) {

        long currentMemberId = AuthUtil.getCurrentMemberId();
        if (currentMemberId != param.getMemberId())  return sendErrorResponse(response, "유효하지 않은 요청입니다.");

        memberService.updateMember(param
        //        , imageFile
        );

        return ApiResponse.of("성공");
    }

    @PatchMapping("/password")
    public ApiResponse<String> changePassword(@RequestBody PasswordChangeRequest request,
                                              HttpServletResponse response) {
        try {
            memberService.updatePassword(request.getOldPassword(), request.getNewPassword());
        }catch (RuntimeException e){
            return sendErrorResponse(response, "유효하지 않은 요청입니다.");
        }

        return ApiResponse.of("성공");
    }

    @PostMapping("/google")
    public ApiResponse<AuthResponse> googleNativeLogin(@RequestBody Map<String,String> request, HttpServletResponse response){
        GoogleIdToken.Payload payload = verifyGoogleIdToken(request.get("idToken")).block();
        if (payload != null) {
            payload.forEach((k,v)-> System.out.println("key:"+k+", value:"+v));
        }

        String providerId = (String) payload.get("sub");
        String email = payload.getEmail();
        String nickname = (String) payload.get("name");
        String aud = (String)payload.getAudience();
        if(!aud.equals(googleAndroidClientId) && !aud.equals(googleClientId)) return sendErrorResponse(response, "제가 발급한 게 아닙니다.");

        System.out.println("nickname:"+nickname);
        MobileSocialLoginParam param = MobileSocialLoginParam.builder()
                .nickname(nickname)
                .email(email)
                .providerId(providerId)
                .provider(SocialProvider.GOOGLE)
                .build();

        MemberDto memberDto = memberService.processMobileSocialLogin(param);

        String accessToken = jwtTokenProvider.generateAccessToken(memberDto);
        String refreshToken = jwtTokenProvider.generateRefreshToken(memberDto.getEmail());


        AuthResponse authResponse =  AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        redisService.saveRefreshToken(memberDto.getEmail(), refreshToken);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(memberDto.getEmail());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return ApiResponse.of(authResponse);

    }

    private Mono<GoogleIdToken.Payload> verifyGoogleIdToken(String idToken) {
        WebClient webClient = WebClient.create("https://oauth2.googleapis.com");
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tokeninfo")
                        .queryParam("id_token", idToken)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    if (response.containsKey("email")) {
                        //response.forEach((k,v)-> System.out.println("key:"+k+", value:"+v));
                        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
                        payload.setEmail((String) response.get("email"));
                        payload.set("name", response.get("name"));
                        payload.set("sub", response.get("sub"));
                        payload.setAudience(response.get("aud"));
                        return payload;
                    }
                    return null;
                })
                .onErrorResume(WebClientResponseException.class, ex -> Mono.empty());
    }

    @PostMapping("/line")
    public ApiResponse<AuthResponse> lineLogin(@RequestBody Map<String,String> request, HttpServletResponse response){
        Map payload = verifyLineIdToken(request.get("idToken")).block();
        if (payload != null) {
            payload.forEach((k,v)-> System.out.println("key:"+k+", value:"+v));
        }

        String providerId = (String) payload.get("sub");
        String email = (String)payload.get("email");
        String nickname = (String) payload.get("name");
        String aud = (String)payload.get("aud");
        if(!aud.equals(lineAndroidClientId)) return sendErrorResponse(response, "제가 발급한 게 아닙니다.");

        System.out.println("nickname:"+nickname);
        MobileSocialLoginParam param = MobileSocialLoginParam.builder()
                .nickname(nickname)
                .email(email)
                .providerId(providerId)
                .provider(SocialProvider.LINE)
                .build();

        MemberDto memberDto = memberService.processMobileSocialLogin(param);

        String accessToken = jwtTokenProvider.generateAccessToken(memberDto);
        String refreshToken = jwtTokenProvider.generateRefreshToken(memberDto.getEmail());


        AuthResponse authResponse =  AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        redisService.saveRefreshToken(memberDto.getEmail(), refreshToken);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(memberDto.getEmail());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return ApiResponse.of(authResponse);

    }

    private Mono<Map> verifyLineIdToken(String idToken) {
        WebClient webClient = WebClient.create("https://api.line.me/oauth2/v2.1");
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/verify")
                        .build())
                .header("Content-Type", "application/x-www-form-urlencoded") // 명시적으로 설정
                .body(BodyInserters.fromFormData("id_token", idToken)
                        .with("client_id", lineAndroidClientId)) // 여러 개의 값 추가 가능
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    if (response.containsKey("email")) {
                        //response.forEach((k,v)-> System.out.println("key:"+k+", value:"+v));
                        return response;
                    }
                    return null;
                })
                .onErrorResume(WebClientResponseException.class, ex -> Mono.empty());
    }

    private ApiResponse sendErrorResponse(HttpServletResponse response, String message) {
        try {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"message\": \"" + message + "\"}");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null; // 클라이언트에는 이미 응답이 전달되었으므로 return null
    }


    @GetMapping("/test")
    public ApiResponse<String> test(){
        return ApiResponse.of("인증인가가 잘 되는 중임");
    }

}
