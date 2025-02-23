package team.klover.server.domain.auth.interfaces;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

@Slf4j
public class AppleUserInfo implements OAuth2UserInfo {

    private Map<String, Object> attributes;

    public AppleUserInfo(Map<String, Object> attributes) {
        this.attributes = (Map<String, Object>) attributes.get("response");
    }

    @Override
    public String getId() {
        return Objects.toString(attributes.get("id"), "");
    }

    @Override
    public String getName() {
        return Objects.toString(attributes.get("nickname"), "");
    }


    @Override
    public String getEmail() {
        return Objects.toString(attributes.get("email"), "");
    }
}
