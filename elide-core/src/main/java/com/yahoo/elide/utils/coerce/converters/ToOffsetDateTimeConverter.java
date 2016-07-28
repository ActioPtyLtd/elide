/*
 * Copyright 2016, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.utils.coerce.converters;

import com.yahoo.elide.core.exceptions.InvalidAttributeException;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang3.ClassUtils;

import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

/**
 * Converter to OffsetDateTime.
 */
public class ToOffsetDateTimeConverter implements Converter {
    /**
     * Convert value to OffsetDateTime.
     *
     * @param cls   class to convert to
     * @param value value to convert
     * @param <T>   return type
     * @return an OffsetDateTime
     */
    @Override
    public <T> T convert(Class<T> cls, Object value) {

        try {
            if (ClassUtils.isAssignable(value.getClass(), CharSequence.class)) {
                return cls.cast(OffsetDateTime.parse((CharSequence) value));
            } else {
                return cls.cast(OffsetDateTime.from((TemporalAccessor) value));
            }
        } catch (DateTimeException e) {
            throw new InvalidAttributeException("Invalid " + cls.getSimpleName() + " value " + value, e);
        }
    }
}
