/*
 * Copyright 2015, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.core;

/**
 * Database interface library.
 */
public interface DataStore {

    /**
     * Load entity dictionary with JPA annotated beans.
     *
     * @param dictionary the dictionary
     */
    void populateEntityDictionary(EntityDictionary dictionary);

    /**
     * Begin transaction.
     *
     * @return the database transaction
     */
    DataStoreTransaction beginTransaction();

    default DataStoreTransaction beginTransaction(DataStoreTransaction parent) {
        return beginTransaction();
    }

    /**
     * Begin read-only transaction.  Default to regular transaction.
     *
     * @return the database transaction
     */
    default DataStoreTransaction beginReadTransaction() {
        return beginTransaction();
    }

    default DataStoreTransaction beginReadTransaction(DataStoreTransaction parent) {
        return beginTransaction(parent);
    }
}
