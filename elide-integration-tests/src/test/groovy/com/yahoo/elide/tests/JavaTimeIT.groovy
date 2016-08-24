/*
 * Copyright 2015, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.tests

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.jayway.restassured.RestAssured
import com.yahoo.elide.core.HttpStatus
import com.yahoo.elide.initialization.AbstractIntegrationTestInitializer
import com.yahoo.elide.initialization.JavaTimeIntegrationTestApplicationResourceConfig
import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import static com.jayway.restassured.RestAssured.given

/**
 * Test serialization and deserialization of Java time classes including string to time type coercion.
 */
class JavaTimeIT extends AbstractIntegrationTestInitializer {
    private final ObjectMapper mapper = new ObjectMapper()

    public JavaTimeIT() {
        super(JavaTimeIntegrationTestApplicationResourceConfig.class);
    }

    @BeforeClass
    public void setup() {
        JavaTimeModule javaTimeModule = new JavaTimeModule()
        this.jsonApiMapper.objectMapper.registerModule(javaTimeModule)
        mapper.registerModule(javaTimeModule)

        def postRequest = given()
                .contentType("application/vnd.api+json")
                .accept("application/vnd.api+json")
                .body('''
                    {
                      "data": {
                        "type": "javaTimeEntity",
                        "id": "1",
                        "attributes": {
                          "instantValue": "2016-08-03T10:15:30.123Z",
                          "localDateValue" : "2016-08-03",
                          "localDateTimeValue" : "2016-08-03T10:15:30",
                          "localTimeValue" : "10:15",
                          "offsetDateTimeValue" : "2016-08-03T10:15:30+08:00",
                          "offsetTimeValue" : "10:15:30+08:00",
                          "yearValue" : "2016",
                          "yearMonthValue" : "2016-08",
                          "zonedDateTimeValue" : "2007-12-03T10:15:30+01:00[Europe/Paris]"
                        }
                      }
                    }
                    ''')
                .post("/javaTimeEntity")
        postRequest.then().statusCode(HttpStatus.SC_CREATED)
    }

    @Test
    public void testDeserializedJavaTimeValues() {
        def result = mapper.readTree(RestAssured
                .get("/javaTimeEntity")
                .asString());
        Assert.assertTrue(result["data"].size() == 1);
        def row = result["data"].get(0)["attributes"]
        Assert.assertEquals(row["instantValue"].asText(), "2016-08-03T10:15:30.123Z")
        Assert.assertEquals(row["localDateValue"].asText(), "2016-08-03")
        Assert.assertEquals(row["localDateTimeValue"].asText(), "2016-08-03T10:15:30")
        Assert.assertEquals(row["localTimeValue"].asText(), "10:15")
        Assert.assertEquals(row["offsetDateTimeValue"].asText(), "2016-08-03T10:15:30+08:00")
        Assert.assertEquals(row["offsetTimeValue"].asText(), "10:15:30+08:00")
        Assert.assertEquals(row["yearValue"].asText(), "2016")
        Assert.assertEquals(row["yearMonthValue"].asText(), "2016-08")

        // Geographic qualifier will be dropped
        Assert.assertEquals(row["zonedDateTimeValue"].asText(),"2007-12-03T10:15:30+01:00")
    }

    @Test
    public void testFilteringJavaTimeValues() {
        def result = mapper.readTree(RestAssured
                .get("/javaTimeEntity?filter[javaTimeEntity.instantValue][gt]=2016-08-03T10:15:30.122Z")
                .asString());
        Assert.assertTrue(result.get("data").size() == 1);
        result = mapper.readTree(RestAssured
                .get("/javaTimeEntity?filter[javaTimeEntity.instantValue][lt]=2016-08-03T10:15:30.122Z")
                .asString());
        Assert.assertTrue(result.get("data").size() == 0);
    }

    @AfterClass
    public void tearDown() {
        def request = RestAssured.delete("/javaTimeEntity/1")
        request.then().statusCode(HttpStatus.SC_NO_CONTENT)
    }
}
