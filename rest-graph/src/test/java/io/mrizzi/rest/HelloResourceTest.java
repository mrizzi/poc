package io.mrizzi.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;

@QuarkusTest
public class HelloResourceTest {

    private static final String PATH = "/hello";

    @Test
    public void testHelloGetEndpoint() {
        given()
          .when().get("/hello")
          .then()
             .statusCode(200)
                .log().body()
             .body("size()", is(2),
                     "", containsInAnyOrder("pluto", "neptune"));
    }

    @Test
    public void testHelloPostEndpoint() {
        given()
          .when().post("/hello")
          .then()
             .statusCode(200)
             .body(is("Hello RESTEasy"));
    }

}