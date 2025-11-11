package org.pulse.npsservice;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusIntegrationTest
public class FeedbackFunctionIT {

    @Test
    public void testFeedbackEndpoint() {
        given()
                .when()
                .get("/api/feedback")
                .then()
                .statusCode(200)
                .body(is("Sending feedback..."));
    }
}
