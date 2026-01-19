package com.prism.statistics.domain.user.vo;

import com.prism.statistics.domain.user.enums.RegistrationId;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Social {

    @Enumerated(EnumType.STRING)
    private RegistrationId registrationId;

    private String socialId;

    public Social(RegistrationId registrationId, String socialId) {
        this.registrationId = registrationId;
        this.socialId = socialId;
    }
}
