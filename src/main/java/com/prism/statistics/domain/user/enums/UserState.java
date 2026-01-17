package com.prism.statistics.domain.user.enums;

public enum UserState {
    ACTIVE, WITHDRAWAL;

    public boolean isActive() {
        return this == UserState.ACTIVE;
    }

    public boolean isWithdrawal() {
        return this == UserState.WITHDRAWAL;
    }
}
