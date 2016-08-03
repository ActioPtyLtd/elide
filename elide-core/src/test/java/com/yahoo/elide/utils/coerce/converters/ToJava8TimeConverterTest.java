/*
 * Copyright 2016, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.utils.coerce.converters;

import com.yahoo.elide.core.exceptions.InvalidAttributeException;
import org.apache.commons.beanutils.Converter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.Year;

import static org.testng.Assert.assertEquals;


public class ToJava8TimeConverterTest {

    private Converter converter;

    @BeforeMethod
    public void setUp() throws Exception {
       this.converter = new ToJava8TimeConverter();
    }

    @Test
    public void testInstantConversion() throws Exception {
        assertEquals(converter.convert(Instant.class, "2007-12-03T10:15:30.00Z"),
                Instant.parse("2007-12-03T10:15:30.00Z"));
    }

    @Test
    public void testYearConversion() throws Exception {
        assertEquals(converter.convert(Year.class, "2001"), Year.of(2001));
    }

    @Test(expectedExceptions = InvalidAttributeException.class)
    public void testMissingNumberValueException() throws Exception {
        converter.convert(OffsetDateTime.class, "Bad");
    }
}
