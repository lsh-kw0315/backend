package team.klover.server.global.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import team.klover.server.global.security.custom.CustomUserDetails;

import java.util.Collection;

public class AuthUtil {

    public static String getCurrentMemberEmail() {
        return getUserDetails().getUsername();
    }

    public static String getCurrentMemberNickname() {
        return getUserDetails().getMember().getNickname();
    }

    public static Collection<? extends GrantedAuthority> getAuth() {
        return getUserDetails().getAuthorities();
    }

    public static Long getCurrentMemberId() {
        return getUserDetails().getMember().getId();
    }

    public static Authentication getAuthentication(){return SecurityContextHolder.getContext().getAuthentication();}

    private static CustomUserDetails getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) throw new RuntimeException("인가가 되지 않았습니다.");

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        if (userDetails == null) throw new RuntimeException("유저가 없습니다.");

        return userDetails;
    }
}
