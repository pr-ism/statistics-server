package com.prism.statistics.domain.user.enums;

import java.util.Arrays;

public enum RegistrationId {

    KAKAO("kakao"),
    GOOGLE("google");

    private final String name;

    RegistrationId(String name) {
        this.name = name;
    }

    public static RegistrationId findBy(String name) {
        return Arrays.stream(RegistrationId.values())
                     .filter(id -> id.name.equalsIgnoreCase(name))
                     .findAny()
                     .orElseThrow(() -> new IllegalArgumentException("지원하는 소셜 로그인 방식이 아닙니다."));
    }

    public static boolean contains(String name) {
        return Arrays.stream(RegistrationId.values())
                     .anyMatch(registrationId -> registrationId.name.equalsIgnoreCase(name));
    }

    public static boolean notContains(String name) {
        return !contains(name);
    }
}
