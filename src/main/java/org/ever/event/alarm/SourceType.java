package org.ever.event.alarm;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SourceType {
    ALARM,
    GATEWAY,
    BUSINESS,
    SCM,
    AUTH,
    PAYMENT,
    UNKNOWN;

    public static SourceType fromString(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        return Arrays.stream(values())
            .filter(sourceType -> sourceType.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(UNKNOWN);
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    public boolean isKnown() {
        return this != UNKNOWN;
    }
}
