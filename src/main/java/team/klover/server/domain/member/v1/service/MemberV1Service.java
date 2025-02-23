package team.klover.server.domain.member.v1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.klover.server.domain.member.auth.dto.SignupRequestDto;
import team.klover.server.domain.member.v1.dto.MemberDto;
import team.klover.server.domain.member.v1.dto.MemberUpdateParam;
import team.klover.server.domain.member.v1.dto.MobileSocialLoginParam;
import team.klover.server.domain.member.v1.entity.Member;
import team.klover.server.domain.member.v1.enums.MemberRole;
import team.klover.server.domain.member.v1.enums.SocialProvider;
import team.klover.server.domain.member.v1.repository.MemberV1Repository;
import team.klover.server.global.util.AuthUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MemberV1Service {
    private final MemberV1Repository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public Member signup(SignupRequestDto requestDto) {
        if (memberRepository.existsByEmail(requestDto.getEmail())) {
            throw new RuntimeException("이미 존재하는 회원임");
        }

        Member member = Member.builder()
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .nickname(requestDto.getNickname())
                .role(MemberRole.USER)
                .socialProvider(SocialProvider.SERVER)
                .build();

        try {
            memberRepository.save(member);
        } catch (Exception e) {
            throw new RuntimeException("가입에 실패함");
        }

        return member;
    }

    public Member findByEmail(String email) {
        return memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일입니다."));
    }

    // 마이페이지 - 사용자 정보 조회
    public MemberDto getMyInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Member member = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일입니다."));

        return MemberDto.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileUrl(member.getProfileUrl())
                .build();
    }

    // 마이페이지 - 비밀번호 수정 (소셜 로그인 사용자는 비밀번호 변경 불가)
    @Transactional
    public void updatePassword(String oldPassword, String newPassword) {
        long id = AuthUtil.getCurrentMemberId();

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다"));

        if (member.getSocialProvider() != SocialProvider.SERVER) {
            throw new RuntimeException("존재하지 않는 회원입니다");
        }

        if (!passwordEncoder.matches(oldPassword, member.getPassword())) {
            throw new RuntimeException("유효하지 않은 요청입니다");
        }

        member.setPassword(passwordEncoder.encode(newPassword));
    }

    public Member getMemberById(Long writerId) {
        return memberRepository.findById(writerId)
                .orElseThrow(() -> new RuntimeException("그런 회원이 없음"));
    }

    public List<Member> getMembersByNickName(List<String> mentionedNames) {
        return memberRepository.findAllByNicknameIn(mentionedNames);
    }

    public Member findLocalMember(String email) {
        return memberRepository.findMemberByEmailAndSocialProvider(email, SocialProvider.SERVER)
                .orElseThrow(() ->  new RuntimeException("그런 회원이 없음"));
    }

    @Transactional
    public void updateMember(MemberUpdateParam param
    //        , MultipartFile imageFile
    ) {
        Member member = memberRepository.findById(param.getMemberId())
                .orElseThrow(() -> new RuntimeException("그런 회원이 없음"));

        /*
        if (param.getProfileUrl() != null) {
            s3Service.deleteFile(param.getProfileUrl());
        }

        String imageUrl = null;
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = s3Service.uploadFile(imageFile, "profile-images");
                param.changeProfileUrl(imageUrl);
            }
        } catch (IOException e) {
            throw new CustomException(ReturnCode.INTERNAL_ERROR);
        }
         */

        member.update(param);
    }

    public MemberDto processMobileSocialLogin(MobileSocialLoginParam param) {
        String password = passwordEncoder.encode(UUID.randomUUID().toString());

        Optional<Member> target = memberRepository.findMemberByEmail(param.getEmail());

        //존재하지 않으면 회원 가입 처리
        if(target.isEmpty()){
            Member member = Member.builder()
                    .email(param.getEmail())
                    .providerId(param.getProviderId())
                    .password(password)
                    .role(MemberRole.USER)
                    .socialProvider(param.getProvider())
                    .nickname(param.getNickname())
                    .build();

            memberRepository.save(member);

            return new MemberDto(member);
        }

        return new MemberDto(target.get());

    }
}
