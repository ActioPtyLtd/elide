/*
 * Copyright 2016, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.initialization;

import com.beust.jcommander.internal.Lists;
import com.yahoo.elide.Elide;
import com.yahoo.elide.audit.AuditLogger;
import com.yahoo.elide.core.EntityDictionary;
import com.yahoo.elide.core.filter.dialect.DefaultFilterDialect;
import com.yahoo.elide.core.filter.dialect.MultipleFilterDialect;
import com.yahoo.elide.core.filter.dialect.RSQLFilterDialect;
import com.yahoo.elide.jsonapi.JsonApiMapper;
import com.yahoo.elide.resources.JsonApiEndpoint;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.HashMap;

/**
 * Typical-use test binder for integration test resource configs.
 */
public class StandardTestBinder extends AbstractBinder {
    private final AuditLogger auditLogger;
    private final ObjectMapperCustomizer objectMapperCustomizer;

    public StandardTestBinder(final AuditLogger auditLogger) {
        this(auditLogger, null);
    }

    public StandardTestBinder(final AuditLogger auditLogger, ObjectMapperCustomizer objectMapperCustomizer) {
        this.auditLogger = auditLogger;
        this.objectMapperCustomizer = objectMapperCustomizer;
    }

    @Override
    protected void configure() {
        // Elide instance
        bindFactory(new Factory<Elide>() {
            @Override
            public Elide provide() {
                EntityDictionary dictionary = new EntityDictionary(new HashMap<>());
                DefaultFilterDialect defaultFilterStrategy = new DefaultFilterDialect(dictionary);
                RSQLFilterDialect rsqlFilterStrategy = new RSQLFilterDialect(dictionary);

                MultipleFilterDialect multipleFilterStrategy = new MultipleFilterDialect(
                        Lists.newArrayList(rsqlFilterStrategy, defaultFilterStrategy),
                        Lists.newArrayList(rsqlFilterStrategy, defaultFilterStrategy)
                );

                JsonApiMapper jsonApiMapper = new JsonApiMapper(dictionary);
                if (objectMapperCustomizer != null) {
                    objectMapperCustomizer.customize(jsonApiMapper.getObjectMapper());
                }

                return new Elide.Builder(AbstractIntegrationTestInitializer.getDatabaseManager())
                    .withAuditLogger(auditLogger)
                    .withJoinFilterDialect(multipleFilterStrategy)
                    .withSubqueryFilterDialect(multipleFilterStrategy)
                    .withEntityDictionary(dictionary)
                    .withJsonApiMapper(jsonApiMapper)
                    .build();
            }

            @Override
            public void dispose(Elide elide) {

            }
        }).to(Elide.class).named("elide");

        // User function
        bindFactory(new Factory<JsonApiEndpoint.DefaultOpaqueUserFunction>() {
            private final Integer user = 1;

            @Override
            public JsonApiEndpoint.DefaultOpaqueUserFunction provide() {
                return v -> user;
            }

            @Override
            public void dispose(JsonApiEndpoint.DefaultOpaqueUserFunction defaultOpaqueUserFunction) {
            }
        }).to(JsonApiEndpoint.DefaultOpaqueUserFunction.class).named("elideUserExtractionFunction");
    }
}
