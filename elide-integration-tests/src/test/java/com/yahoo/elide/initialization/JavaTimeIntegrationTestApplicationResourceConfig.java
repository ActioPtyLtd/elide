/*
 * Copyright 2016, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.initialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yahoo.elide.audit.TestAuditLogger;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Resource configuration for Java time integration tests (requires Jackson JavaTime module)
 */
public class JavaTimeIntegrationTestApplicationResourceConfig extends ResourceConfig {
    public JavaTimeIntegrationTestApplicationResourceConfig() {
        register(new StandardTestBinder(new TestAuditLogger(), (ObjectMapper objectMapper) -> {
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        }));
    }
}
