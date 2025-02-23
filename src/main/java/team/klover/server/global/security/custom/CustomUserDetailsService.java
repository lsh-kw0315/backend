package team.klover.server.global.security.custom;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import team.klover.server.domain.member.v1.entity.Member;
import team.klover.server.domain.member.v1.repository.MemberV1Repository;

@Service
@RequiredArgsConstructor
// 인증에 필요한 사용자 정보를 UserDetails 객체로 로드하는 역할
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberV1Repository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        return new CustomUserDetails(member);
    }
}
