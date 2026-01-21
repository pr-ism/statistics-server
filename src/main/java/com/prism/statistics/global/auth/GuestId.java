package com.prism.statistics.global.auth;

public record GuestId(Long userId) {

    public boolean isGuest() {
        return this.userId() == null;
    }
}
