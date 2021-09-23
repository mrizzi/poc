package io.mrizzi.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.iterableWithSize;

@QuarkusTest
public class WindupResourceTest {

    private static final String PATH = "/windup";

    @Test
    public void testWindupGetIssueCategoryEndpoint() {
        given()
          .when().get(String.format("%s/issueCategory", PATH))
          .then()
             .statusCode(200)
                .log().body()
             .body("", iterableWithSize(6),
                     "name", containsInAnyOrder("Information", "Migration Optional", "Cloud Mandatory", "Migration Mandatory", "Cloud Optional", "Migration Potential"),
                     "categoryID", containsInAnyOrder("information", "optional", "cloud-mandatory", "mandatory", "cloud-optional", "potential"));
    }

    @Test
    public void testWindupGetIssueEndpoint() {
        given()
          .when().get(String.format("%s/issue", PATH))
          .then()
             .statusCode(200)
             .body("", iterableWithSize(35));
    }

    @Test
    public void testWindupPutAnalysisEndpoint() {
        given()
          .when().put(String.format("%s/application/%d/analysis/", PATH, 0L))
          .then()
             .statusCode(202)
                .log().all();
    }
}
