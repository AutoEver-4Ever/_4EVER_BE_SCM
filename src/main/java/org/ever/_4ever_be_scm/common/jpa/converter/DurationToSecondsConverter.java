package org.ever._4ever_be_scm.common.jpa.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Duration;

@Converter(autoApply = false)
public class DurationToSecondsConverter implements AttributeConverter<Duration, Long> {
    @Override
    public Long convertToDatabaseColumn(Duration attribute) {
        if (attribute == null) return null;
        return attribute.getSeconds();
    }

    @Override
    public Duration convertToEntityAttribute(Long dbData) {
        if (dbData == null) return null;
        return Duration.ofSeconds(dbData);
    }
}

