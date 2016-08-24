/*
 * Copyright 2016, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package example;


import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.SharePermission;
import com.yahoo.elide.security.checks.prefab.Role;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Include(rootLevel = true)
@SharePermission(all = {Role.ALL.class})
@Getter
@Setter
public class HeadOfGovernment {
    private long id;
    private String name;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }
}