package io.mrizzi.qute;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
public class ItemResourceTest {

    @Test
    public void testGetEndpoint() {
        given()
                .when()
                .get("/item/1")
                .then()
                .statusCode(200)
                .body(containsString("<h1>Product <b>Pear</b></h1>"),
                        containsString("<div>Price: 0.")
                );
    }

    @Test
    public void testGetAllEndpoint() {
        given()
                .when()
                .get("/item/")
                .then()
                .statusCode(200)
                .body(containsString("<h1>List of <b>4</b> Items</h1>"),
                        containsString("<li>\n" +
                                "            <a href=\"/item/3\">Pineapple</a>: 0.")
                );
    }

}
