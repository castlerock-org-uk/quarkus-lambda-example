package com.uk.savient;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
public class GreetingTest
{
    @Test
    public void testJaxrs() {
        RestAssured.when().get("/greetings/hello").then()
                .contentType("text/plain")
                .body(equalTo("hello from jaxrs"));
    }

    @Test
    public void testEvenin() {
        RestAssured.when().get("/greetings/evening").then()
                .contentType("text/plain")
                .body(equalTo("evenin squire!"));
    }


}
