package utils;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

public final class SchemaValidator {
    private SchemaValidator() {
    }

    public static void validateBooking(Response response) {
        response.then().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/booking-schema.json"));
    }

    public static void validateCreatedBooking(Response response) {
        response.then().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/booking-created-schema.json"));
    }
}
