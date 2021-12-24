package io.mrizzi.qute;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
public class SomePageTest {

    @Test
    public void testEndpoint() {
        given()
                .when().get("/some-page")
                .then()
                .statusCode(200)
                .body(containsString("<h1>Hello <b>Qute</b></h1>"));

        given()
                .when().get("/some-page?name=Doc")
                .then()
                .statusCode(200)
                .body(containsString("<h1>Hello <b>Doc</b></h1>"));
    }

}
