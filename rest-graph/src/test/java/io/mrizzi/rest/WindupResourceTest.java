package io.mrizzi.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.not;

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
    @Disabled
    public void testWindupGetIssueEndpoint() {
        given()
            .queryParam("applicationId", 0)
            .when().get(String.format("%s/issue", PATH))
          .then()
             .statusCode(200)
             .body("size()", is(35));
    }

    @Test
    public void testWindupPutAnalysisEndpoint() {
        given()
          .when().put(String.format("%s/application/%d/analysis/", PATH, 0L))
          .then()
                .statusCode(202);

        given()
                .when().get(String.format("%s/issue", PATH))
                .then()
                .statusCode(200)
                .body("", iterableWithSize(35));

        given()
          .when().put(String.format("%s/application/%d/analysis/", PATH, 1L))
          .then()
                .statusCode(202);

        given()
                .when().get(String.format("%s/issue", PATH))
                .then()
                .statusCode(200)
                .body("", iterableWithSize(70),
                        "findAll { issue -> issue.applicationId == '0' }", iterableWithSize(35),
                        "findAll { issue -> issue.applicationId == '1' }", iterableWithSize(35));

        given()
                .queryParam("applicationId", 0)
                .when().get(String.format("%s/issue", PATH))
                .then()
                .statusCode(200)
                .body("", iterableWithSize(35),
                        "vertices_out.EffortReportModel-issueCategory.vertices[0].categoryID", not(emptyOrNullString()),
                        "vertices_out.file.vertices[0].filePath", not(emptyOrNullString()));

        given()
                .when().put(String.format("%s/application/%d/analysis/", PATH, 0L))
                .then()
                .statusCode(202);

        given()
                .queryParam("applicationId", 0)
                .when().get(String.format("%s/issue", PATH))
                .then()
                .statusCode(200)
                .body("size()", is(35),
                        "vertices_out.EffortReportModel-issueCategory.vertices[0].categoryID", not(emptyOrNullString()),
                        "vertices_out.file.vertices[0].filePath", not(emptyOrNullString()));

    }

    @Test
    public void testWindupPostAnalysisEndpoint() {
        given()
                .multiPart("application", new File("/home/mrizzi/Tools/windup/sample/input/jee-example-app-1.0.0.ear"))
                .multiPart("applicationFileName","foo.ear")
                .multiPart("targets","eap7,cloud-readiness,quarkus,rhr")
                .when()
                .log().all()
                .post(String.format("%s/analysis/", PATH))
                .then()
                .log().all()
                .statusCode(201);
    }
}
