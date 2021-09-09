package io.mrizzi.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.iterableWithSize;

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
             .body("", iterableWithSize(6),
                     "name", containsInAnyOrder("Information", "Migration Optional", "Cloud Mandatory", "Migration Mandatory", "Cloud Optional", "Migration Potential"),
                     "categoryID", containsInAnyOrder("information", "optional", "cloud-mandatory", "mandatory", "cloud-optional", "potential"));
    }
}
