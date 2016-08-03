/*
 * Copyright 2016, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.initialization;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * To support customization of the Jackson ObjectMapper used by Elide within tests, eg to test
 * serialization/deserialization of Java 8 time classes which requires an additional Jackson module.
 */
public interface ObjectMapperCustomizer {
    void customize(ObjectMapper objectMapper);
}
