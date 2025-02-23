package com.ll.server.global.security.custom;

import com.ll.server.domain.member.entity.Member;
import com.ll.server.domain.member.repository.MemberRepository;
import com.ll.server.global.response.enums.ReturnCode;
import com.ll.server.global.response.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
// 인증에 필요한 사용자 정보를 UserDetails 객체로 로드하는 역할
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new CustomException(ReturnCode.NOT_FOUND_ENTITY));

        return new CustomUserDetails(member);
    }
}
