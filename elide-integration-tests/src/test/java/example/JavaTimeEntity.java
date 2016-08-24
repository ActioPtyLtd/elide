/*
 * Copyright 2016, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package example;

import com.yahoo.elide.annotation.Include;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;

/**
 * Entity containing various Java 8 time types.
 */
@Entity
@Include(rootLevel = true)
@Getter
@Setter
public class JavaTimeEntity {
    private Long id;
    private Instant instantValue;
    private LocalDate localDateValue;
    private LocalDateTime localDateTimeValue;
    private LocalTime localTimeValue;
    private OffsetDateTime offsetDateTimeValue;
    private OffsetTime offsetTimeValue;
    private Year yearValue;
    private YearMonth yearMonthValue;
    private ZonedDateTime zonedDateTimeValue;

    @Id
    public Long getId() {
        return id;
    }
}
