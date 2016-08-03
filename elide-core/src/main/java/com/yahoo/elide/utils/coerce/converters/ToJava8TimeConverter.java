/*
 * Copyright 2016, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.utils.coerce.converters;

import com.yahoo.elide.core.exceptions.InvalidAttributeException;
import org.apache.commons.beanutils.Converter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.Map;

/**
 * For converting text values to Java 8 time types. Only the Java 8 time classes having a parse(CharSequence) method
 * are supported. For example,JapaneseDate is not supported.
 */
public class ToJava8TimeConverter implements Converter {

    private interface Parser {
        Temporal parse(CharSequence text);
    }

    private Map<Class, Parser> temporalParserMap = new HashMap<>();

    public ToJava8TimeConverter() {
        temporalParserMap.put(Instant.class, Instant::parse);
        temporalParserMap.put(LocalDate.class, LocalDate::parse);
        temporalParserMap.put(LocalDateTime.class, LocalDateTime::parse);
        temporalParserMap.put(LocalTime.class, LocalTime::parse);
        temporalParserMap.put(OffsetDateTime.class, OffsetDateTime::parse);
        temporalParserMap.put(OffsetTime.class, OffsetTime::parse);
        temporalParserMap.put(Year.class, Year::parse);
        temporalParserMap.put(YearMonth.class, YearMonth::parse);
        temporalParserMap.put(ZonedDateTime.class, ZonedDateTime::parse);
    }

    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        return (CharSequence.class.isAssignableFrom(sourceType) && temporalParserMap.containsKey(targetType));
    }

    /**
     * Convert a text value to a Java 8 time type. The text value must comply with the format used with the specific
     * parse method.
     *
     * @param targetCls class to convert to
     * @param value value to convert
     * @param <T> object type
     * @return converted object
     */
    @Override
    public <T> T convert(Class<T> targetCls, Object value) {
        try {
            Parser parser = temporalParserMap.get(targetCls);
            return (T) parser.parse((CharSequence) value);
        } catch (DateTimeParseException e) {
            throw new InvalidAttributeException("Cannot parse " + targetCls.getSimpleName() + " value " + value, e);
        }
    }
}
