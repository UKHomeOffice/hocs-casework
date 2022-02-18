package uk.gov.digital.ho.hocs.casework.security;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum AccessLevel {
    UNSET(0), SUMMARY(1), READ(2), RESTRICTED_OWNER(3), WRITE(4), OWNER(5), CASE_ADMIN(6);

    private int level;

    AccessLevel(int level) {
        this.level = level;
    }

    public static AccessLevel from(int value) {
        return Arrays.stream(values())
                .filter(level -> level.level == value)
                .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
