package com.prism.statistics.domain.user;

@FunctionalInterface
public interface NicknameIndexGenerator {

    int nextInt(int bound);
}
