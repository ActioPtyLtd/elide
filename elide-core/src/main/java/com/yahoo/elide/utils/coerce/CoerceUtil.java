/*
 * Copyright 2015, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.utils.coerce;

import com.yahoo.elide.core.exceptions.InvalidAttributeException;
import com.yahoo.elide.core.exceptions.InvalidValueException;
import com.yahoo.elide.utils.coerce.converters.FromMapConverter;
import com.yahoo.elide.utils.coerce.converters.ToEnumConverter;
import com.yahoo.elide.utils.coerce.converters.ToInstantConverter;
import com.yahoo.elide.utils.coerce.converters.ToOffsetDateTimeConverter;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Class for coercing a value to a target class.
 */
public class CoerceUtil {

    private static final ToEnumConverter TO_ENUM_CONVERTER = new ToEnumConverter();
    private static final FromMapConverter FROM_MAP_CONVERTER = new FromMapConverter();
    private static final ToInstantConverter TO_INSTANT_CONVERTER = new ToInstantConverter();
    private static final ToOffsetDateTimeConverter TO_OFFSETDATETIME_CONVERTER = new ToOffsetDateTimeConverter();

    //static block for setup and registering new converters
    static {
        setup();
    }

    /**
     * Convert value to target class.
     *
     * @param value value to convert
     * @param cls class to convert to
     * @return coerced value
     */
    public static Object coerce(Object value, Class<?> cls) {
        if (value == null || cls == null || cls.isAssignableFrom(value.getClass())) {
            return value;
        }

        try {
            return ConvertUtils.convert(value, cls);

        } catch (ConversionException | InvalidAttributeException | IllegalArgumentException e) {
            throw new InvalidValueException(value, e.getMessage());
        }
    }

    /**
     * Perform CoerceUtil setup.
     */
    private static void setup() {
        BeanUtilsBean.setInstance(new BeanUtilsBean(new ConvertUtilsBean() {

            @Override
            /*
             * Overriding lookup to execute enum converter if target is enum
             * or map convert if source is map
             * or convert to Java 8 Instant
             */
            public Converter lookup(Class<?> sourceType, Class<?> targetType) {
                if (targetType.isEnum()) {
                    return TO_ENUM_CONVERTER;
                } else if (Map.class.isAssignableFrom(sourceType)) {
                    return FROM_MAP_CONVERTER;
                } else if (targetType.equals(Instant.class)) {
                    return TO_INSTANT_CONVERTER;
                } else if (targetType.equals(OffsetDateTime.class)) {
                    return TO_OFFSETDATETIME_CONVERTER;
                } else {
                    return super.lookup(sourceType, targetType);
                }
            }
        }));
    }
}
