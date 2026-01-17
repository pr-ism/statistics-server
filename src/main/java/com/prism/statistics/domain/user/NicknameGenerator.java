package com.prism.statistics.domain.user;

import com.prism.statistics.domain.user.vo.Nickname;
import java.util.List;

public class NicknameGenerator {

    private final List<String> adjectives;
    private final List<String> colors;

    public static NicknameGenerator of(List<String> adjectives, List<String> colors) {
        validateAdjectives(adjectives);
        validateColors(colors);

        return new NicknameGenerator(adjectives, colors);
    }

    private NicknameGenerator(List<String> adjectives, List<String> colors) {
        this.adjectives = adjectives;
        this.colors = colors;
    }

    public Nickname generate(NicknameIndexGenerator nicknameIndexGenerator) {
        if (nicknameIndexGenerator == null) {
            throw new IllegalArgumentException("닉네임 인덱스 생성 컴포넌트가 필요합니다.");
        }

        String targetAdjective = getElement(adjectives, nicknameIndexGenerator);
        String targetColor = getElement(colors, nicknameIndexGenerator);

        return Nickname.create(targetAdjective.concat(targetColor));
    }

    private String getElement(List<String> target, NicknameIndexGenerator nicknameIndexGenerator) {
        int index = nicknameIndexGenerator.nextInt(target.size());

        return target.get(index);
    }

    private static void validateAdjectives(List<String> adjectives) {
        if (adjectives == null || adjectives.isEmpty()) {
            throw new IllegalArgumentException("닉네임 형용사 목록이 비어있습니다.");
        }
    }

    private static void validateColors(List<String> colors) {
        if (colors == null || colors.isEmpty()) {
            throw new IllegalArgumentException("닉네임 색상 목록이 비어있습니다.");
        }
    }
}
