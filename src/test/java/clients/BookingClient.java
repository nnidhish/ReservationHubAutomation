package clients;

import io.restassured.response.Response;
import utils.ConfigReader;

import java.util.Map;

public class BookingClient extends BaseClient {
    public Response ping() {
        int attempts = ConfigReader.getInt("health.retry.count");
        long delay = ConfigReader.getInt("health.retry.delay.ms");
        Response lastResponse = null;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            lastResponse = requestSpec().when().get("/ping");
            if (lastResponse.statusCode() == 201) {
                return lastResponse;
            }
            sleep(delay);
        }

        return lastResponse;
    }

    public Response listBookings() {
        return requestSpec().when().get("/booking");
    }

    public Response listBookings(Map<String, Object> queryParams) {
        return requestSpec().queryParams(queryParams).when().get("/booking");
    }

    public Response getBooking(int bookingId) {
        return requestSpec().when().get("/booking/{id}", bookingId);
    }

    public Response createBooking(Object payload) {
        return requestSpec().body(payload).when().post("/booking");
    }

    public Response updateBooking(int bookingId, Object payload, String token) {
        return requestSpec()
                .cookie("token", token)
                .body(payload)
                .when()
                .put("/booking/{id}", bookingId);
    }

    public Response updateBookingWithoutAuth(int bookingId, Object payload) {
        return requestSpec()
                .body(payload)
                .when()
                .put("/booking/{id}", bookingId);
    }

    public Response patchBooking(int bookingId, Object payload, String token) {
        return requestSpec()
                .cookie("token", token)
                .body(payload)
                .when()
                .patch("/booking/{id}", bookingId);
    }

    public Response deleteBooking(int bookingId, String token) {
        return requestSpec()
                .cookie("token", token)
                .when()
                .delete("/booking/{id}", bookingId);
    }

    public Response deleteBookingWithoutAuth(int bookingId) {
        return requestSpec().when().delete("/booking/{id}", bookingId);
    }

    private void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for API health retry", exception);
        }
    }
}
