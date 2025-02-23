package team.klover.server.domain.member.v1.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import team.klover.server.domain.member.v1.dto.MemberUpdateParam;
import team.klover.server.domain.member.v1.enums.MemberRole;
import team.klover.server.domain.member.v1.enums.SocialProvider;
import team.klover.server.global.common.entity.BaseEntity;

@Entity
@Getter
@Setter
@Table(name = "members")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Member extends BaseEntity {
    private String email;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    private String password;

    @Enumerated(EnumType.STRING)
    private SocialProvider socialProvider;
    private String providerId;

    private String nickname;

    private String profileUrl;

    private String country;

    public void update(MemberUpdateParam param) {
        profileUrl = param.getProfileUrl();

        if(param.getNickname()!=null && !param.getNickname().isBlank() && !param.getNickname().equals(nickname)) {
            nickname = param.getNickname();
        }

        if(param.getCountry()!=null && !param.getCountry().isBlank() && !param.getCountry().equals(country)){
            country = param.getCountry();
        }
    }
}
