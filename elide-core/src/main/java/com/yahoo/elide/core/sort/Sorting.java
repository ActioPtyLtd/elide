/*
 * Copyright 2015, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.core.sort;

import com.yahoo.elide.core.EntityDictionary;
import com.yahoo.elide.core.exceptions.InvalidValueException;
import lombok.ToString;

import javax.ws.rs.core.MultivaluedMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generates a simple wrapper around the sort fields from the JSON-API GET Query.
 */
@ToString
public class Sorting {

    /**
     * Denotes the intended sort type from json-api field.
     */
    public enum SortOrder { asc, desc }

    private static final Sorting DEFAULT_EMPTY_INSTANCE = new Sorting(null);
    private final Map<String, SortOrder> sortRules = new LinkedHashMap<>();

    /**
     * Constructs a new Sorting instance.
     * @param sortingRules The map of sorting rules
     */
    public Sorting(final Map<String, SortOrder> sortingRules) {
        if (sortingRules != null && !sortingRules.isEmpty()) {
            sortRules.putAll(sortingRules);
        }
    }

    /**
     * Checks to see if the sorting rules are valid for the given JPA class.
     * @param entityClass The target jpa entity
     * @param dictionary The elide entity dictionary
     * @param <T> The Type of the target entity
     * @return The validity of the sorting rules on the target class
     * @throws InvalidValueException when sorting values are not valid for the jpa entity
     */
    public <T> boolean hasValidSortingRules(final Class<T> entityClass,
                                        final EntityDictionary dictionary) throws InvalidValueException {

        sortRules.keySet().stream().forEachOrdered(sortRule -> {

            Class<?> precedingEntityClass = entityClass;

            final String[] keyParts = sortRule.split("\\.");

            for (int i = 0; i < keyParts.length; ++i) {
                if (i == (keyParts.length - 1)) {
                    // Last part of the key must be an attribute of the preceding entity in the path
                        if (!dictionary.getAttributes(precedingEntityClass).contains(keyParts[i])) {
                        throw new InvalidValueException(precedingEntityClass.getSimpleName()
                                + " doesn't contain the field " + keyParts[i]);
                    }
                }
                else {
                    // Preceding parts in the key must be valid entities related to the target entity
                    precedingEntityClass = dictionary.getParameterizedType(precedingEntityClass, keyParts[i]);
                    if (precedingEntityClass == null) {
                        throw new InvalidValueException(entityClass.getSimpleName()
                                + " doesn't have relationship " + keyParts[i]);
                    }
                }
            }
        });
        return true;
    }

    /**
     * Given the sorting rules validate sorting rules against the entities bound to the entityClass.
     * @param entityClass  The root class for sorting (eg. /book?sort=-title this would be package.Book)
     * @param dictionary The elide entity dictionary
     * @param <T> The entityClass
     * @return The valid sorting rules - validated through the entity dictionary, or empty dictionary
     * @throws InvalidValueException when sorting values are not valid for the jpa entity
     */
    public <T> Map<String, SortOrder> getValidSortingRules(final Class<T> entityClass,
                                                           final EntityDictionary dictionary)
            throws InvalidValueException {
        hasValidSortingRules(entityClass, dictionary);
        return sortRules;
    }

    /**
     * @return Fetches the base rules, ignoring validation against an entity class.
     */
    public Map<String, SortOrder> getSortingRules() {
        return this.sortRules;
    }

    /**
     * Informs if the structure is default instance.
     * @return true if this instance is empty - no sorting rules
     */
    public boolean isDefaultInstance() {
        return this.sortRules.isEmpty();
    }

    /**
     * Given the query params on the GET request, collect possible sorting rules.
     * @param queryParams The query params on the request.
     * @return The Sorting instance (default or specific).
     */
    public static Sorting parseQueryParams(final MultivaluedMap<String, String> queryParams) {
        final Map<String, SortOrder> sortingRules = new LinkedHashMap<>();
        queryParams.entrySet().stream()
                .filter(entry -> entry.getKey().equals("sort"))
                .forEachOrdered(entry -> {
                    String sortKey = entry.getValue().get(0);
                    if (sortKey.contains(",")) {
                        for (String sortRule : sortKey.split(",")) {
                            parseSortingRule(sortRule, sortingRules);
                        }
                    } else {
                        parseSortingRule(sortKey, sortingRules);
                    }
                });
        return sortingRules.isEmpty() ? DEFAULT_EMPTY_INSTANCE : new Sorting(sortingRules);
    }

    /**
     * Internal helper method to parse sorting rule strings.
     * @param sortingRule The string from the queryParams
     * @param sortingRules The final shared reference to the sortingRules map
     */
    private static void parseSortingRule(String sortingRule, final Map<String, SortOrder> sortingRules) {
        boolean isDesc = false;
        char firstCharacter = sortingRule.charAt(0);
        if (firstCharacter == '-') {
            isDesc = true;
            sortingRule = sortingRule.substring(1);
        }
        if (firstCharacter == '+') {
            // json-api spec supports asc by default, there is no need to explicitly support +
            sortingRule = sortingRule.substring(1);
        }
        sortingRules.put(sortingRule, isDesc ? SortOrder.desc : SortOrder.asc);
    }

    /**
     * Get the default final empty instance.
     * @return The default empty instance.
     */
    public static Sorting getDefaultEmptyInstance() {
        return DEFAULT_EMPTY_INSTANCE;
    }
}
