/*
 * Copyright 2016, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.datastores.hibernate5.filter;

import com.yahoo.elide.core.EntityDictionary;
import com.yahoo.elide.core.RequestScope;
import com.yahoo.elide.core.exceptions.InvalidValueException;
import com.yahoo.elide.core.filter.Predicate;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Explorer intended to construct criteria from filtering for constraining object loading.
 */
public class CriteriaExplorer {
    private final Criterion rootCriterion;
    private final CriterionFilterOperation filterOperation;
    private final RequestScope requestScope;
    private final EntityDictionary dictionary;
    private final Class<?> loadClass;

    /**
     * Constructor.
     *
     * @param loadClass Root class being loaded
     * @param requestScope Request scope
     * @param existing Pre-existing criterion related to loadClass
     */
    public CriteriaExplorer(Class<?> loadClass, RequestScope requestScope, Criterion existing) {
        this.filterOperation = new CriterionFilterOperation();
        this.requestScope = requestScope;
        this.dictionary = requestScope.getDictionary();
        this.rootCriterion = buildRootCriterion(loadClass, existing);
        this.loadClass = loadClass;
    }

    /**
     * Build a set of criteria from sessionCriteria.
     *
     * NOTE: It is assumed that sessionCriteria is a criteria corresponding to the "loadClass"
     *       that this was instantiated with.
     *
     * @param sessionCriteria Session criteria to update
     * @param session Session to create criteria on
     */
    public void buildCriteria(final Criteria sessionCriteria, final Session session) {
        if (rootCriterion != null) {
            sessionCriteria.add(rootCriterion);
        }

        Map<String, List<Criteria>> filterCriteriaChainMap = new HashMap<>();

        for (Map.Entry<String, Set<Predicate>> entry : requestScope.getPredicates().entrySet()) {
            String criteriaPath = entry.getKey();
            Set<Predicate> predicates = entry.getValue();

            String[] objects = criteriaPath.split("\\.");

            List<Criteria> criteriaChain = new ArrayList<>();

            Criteria criteria;
            Class filterClass = dictionary.getEntityClass(objects[0]);
            if (loadClass.equals(filterClass)) {
                criteria = sessionCriteria;
            } else {
                criteria = session.createCriteria(filterClass);
            }
            criteriaChain.add(criteria);

            for (int i = 1 ; i < objects.length ; ++i) {
                // Give the related entity an alias so that sorting can use this alias
                criteria = criteria.createCriteria(objects[i], objects[i]);
                criteriaChain.add(criteria);
                filterCriteriaChainMap.put(criteria.getAlias(), criteriaChain);
            }
            criteria.add(filterOperation.applyAll(predicates));

        }

        // Need to create any additional criteria required for sorting. Hibernate does not allow duplicate joins
        // between entities, so criteria created for filtering must be reused for sorting and also must exist in the
        // same order.
        requestScope.getSorting().getSortingRules().keySet().forEach(sortingRule -> {
            final String[] keyParts = sortingRule.split("\\.");

            // Check if any criteria have already been created for entities in the sorting rule.
            // Last part of the sort key should be an attribute, so exclude that from the check.
            int matchingKeyPartIndex = -1;
            Criteria precedingCriteria = null;
            for (int i = keyParts.length - 2; i >= 0; i--) {
                List<Criteria> filterCriteriaChain = filterCriteriaChainMap.get(keyParts[i]);
                if (filterCriteriaChain != null) {
                    // Need to verify that the criteria chain used with filtering matches that required for
                    // sorting. Exclude first element in the criteria chain as that is the primary entity which
                    // should not be in the sort keys.
                    int matchingCriteriaIndex = -1;
                    for (int j = filterCriteriaChain.size() - 1; j > 0; j--) {
                        // Find the element in the chain which matches this sort key part
                        if (matchingCriteriaIndex < 0) {
                            if (filterCriteriaChain.get(j).getAlias().equals(keyParts[i])) {
                                // Filter criteria chain must have as many elements as the sort rule key parts
                                // from the point where there is a match, allowing for the fact that the filter
                                // criteria chain also includes the primary entity criteria.
                                if (j != i + 1) {
                                    throw new InvalidValueException(sortingRule
                                            + " must match filter key chain");
                                }
                                matchingCriteriaIndex = j;
                                matchingKeyPartIndex = i;
                                precedingCriteria = filterCriteriaChain.get(j);
                            }
                        }
                        else {
                            // Continue checking that filter criteria chain matches the sort keys
                            if (!filterCriteriaChain.get(j).getAlias()
                                    .equals(keyParts[i - matchingCriteriaIndex + j])) {
                                throw new InvalidValueException(sortingRule
                                        + " must match filter key chain");
                            }
                        }
                    }
                    if (matchingCriteriaIndex > 0) {
                        break;
                    }
                }
            }

            if (precedingCriteria == null) {
                precedingCriteria = sessionCriteria;
            }

            // Create criteria for all remaining sort key parts for which there is not yet a criteria.
            // Also set the alias to the key part so that it will match the sort rule.
            for (int i = matchingKeyPartIndex + 1; i < keyParts.length - 1; i++) {
                precedingCriteria = precedingCriteria.createCriteria(keyParts[i], keyParts[i]);
            }
        });
    }

    /**
     * Build root criteria object.
     *
     * @param loadClass Root class being loaded
     * @param existing Pre-existing criterion related to loadClass
     * @return Criterion for root class
     */
    private Criterion buildRootCriterion(final Class<?> loadClass, final Criterion existing) {
        String type = dictionary.getJsonAliasFor(loadClass);
        return CriterionFilterOperation.andWithNull(existing,
                filterOperation.applyAll(requestScope.getPredicatesOfType(type)));
    }
}
