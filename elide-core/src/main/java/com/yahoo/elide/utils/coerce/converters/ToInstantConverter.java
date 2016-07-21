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
import java.time.Instant;
import java.time.temporal.TemporalAccessor;

/**
 * Converter to Instant.
 */
public class ToInstantConverter implements Converter {
    /**
     * Convert value to Instant.
     *
     * @param cls   class to convert to
     * @param value value to convert
     * @param <T>   return type
     * @return an Instant
     */
    @Override
    public <T> T convert(Class<T> cls, Object value) {

        try {
            if (ClassUtils.isAssignable(value.getClass(), CharSequence.class)) {
                return (T) Instant.parse((CharSequence) value);
            } else {
                return (T) Instant.from((TemporalAccessor) value);
            }
        } catch (DateTimeException e) {
            throw new InvalidAttributeException("Invalid " + cls.getSimpleName() + " value " + value, e);
        }
    }
}
