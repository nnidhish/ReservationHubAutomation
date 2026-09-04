package context;

import io.restassured.response.Response;
import models.Booking;

import java.util.Map;

public class TestContext {
    private int bookingId;
    private String token;
    private Response response;
    private Object payload;
    private Booking currentBooking;
    private Map<String, Object> partialUpdate;

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public Booking getCurrentBooking() {
        return currentBooking;
    }

    public void setCurrentBooking(Booking currentBooking) {
        this.currentBooking = currentBooking;
        this.payload = currentBooking;
    }

    public Map<String, Object> getPartialUpdate() {
        return partialUpdate;
    }

    public void setPartialUpdate(Map<String, Object> partialUpdate) {
        this.partialUpdate = partialUpdate;
        this.payload = partialUpdate;
    }
}
