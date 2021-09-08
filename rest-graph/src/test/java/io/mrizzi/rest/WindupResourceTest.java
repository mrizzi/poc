package io.mrizzi.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class WindupResourceTest {

    private static final String PATH = "/hello";

    @Test
    public void testWindupGetEndpoint() {
        given()
          .when().get("/windup")
          .then()
             .statusCode(200)
                .log().body()
             .body("issue_categories_size", is(6),
                     "total_vertex_count", is(10924),
                     "issues_category_description", is("Information"));
    }
}
