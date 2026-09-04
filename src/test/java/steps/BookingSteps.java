package steps;

import clients.AuthClient;
import clients.BookingClient;
import context.TestContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import models.Booking;
import utils.ConfigReader;
import utils.TestDataFactory;

import java.util.Map;

public class BookingSteps {
    private final TestContext context;
    private final BookingClient bookingClient;
    private final AuthClient authClient;

    public BookingSteps(TestContext context) {
        this.context = context;
        this.bookingClient = new BookingClient();
        this.authClient = new AuthClient();
    }

    @When("I check the API health")
    public void checkApiHealth() {
        context.setResponse(bookingClient.ping());
    }

    @When("I list booking ids")
    public void listBookingIds() {
        context.setResponse(bookingClient.listBookings());
    }

    @When("I list booking ids filtered by the created booking's firstname and lastname")
    public void listBookingIdsFilteredByCreatedBookingName() {
        Booking booking = context.getCurrentBooking();
        context.setResponse(bookingClient.listBookings(Map.of(
                "firstname", booking.getFirstname(),
                "lastname", booking.getLastname()
        )));
    }

    @When("I list booking ids filtered by the created booking's checkin and checkout dates")
    public void listBookingIdsFilteredByCreatedBookingDates() {
        Booking booking = context.getCurrentBooking();
        context.setResponse(bookingClient.listBookings(Map.of(
                "checkin", booking.getBookingdates().getCheckin(),
                "checkout", booking.getBookingdates().getCheckout()
        )));
    }

    @When("I list booking ids filtered by firstname {string} and lastname {string}")
    public void listBookingIdsFilteredByGivenName(String firstname, String lastname) {
        context.setResponse(bookingClient.listBookings(Map.of(
                "firstname", firstname,
                "lastname", lastname
        )));
    }

    @Given("I have a valid booking payload")
    public void haveValidBookingPayload() {
        context.setCurrentBooking(TestDataFactory.validBooking());
    }

    @Given("I have an invalid booking payload for {string}")
    public void haveInvalidBookingPayload(String invalidCase) {
        context.setPayload(TestDataFactory.invalidBookingPayload(invalidCase));
    }

    @Given("I have created a booking")
    @Given("I have created a booking for authorization checks")
    public void haveCreatedBooking() {
        Booking booking = TestDataFactory.validBooking();
        context.setCurrentBooking(booking);
        context.setResponse(bookingClient.createBooking(booking));
        context.setBookingId(context.getResponse().jsonPath().getInt("bookingid"));
    }

    @Given("I use booking id {int}")
    public void useBookingId(int bookingId) {
        context.setBookingId(bookingId);
    }

    @And("I have a valid updated booking payload")
    public void haveValidUpdatedBookingPayload() {
        context.setCurrentBooking(TestDataFactory.updatedBooking());
    }

    @And("I have a valid partial booking update")
    public void haveValidPartialBookingUpdate() {
        Map<String, Object> update = TestDataFactory.partialUpdate();
        context.setPartialUpdate(update);
    }

    @When("I create the booking")
    public void createBooking() {
        context.setResponse(bookingClient.createBooking(context.getPayload()));
        if (context.getResponse().statusCode() == 200) {
            context.setBookingId(context.getResponse().jsonPath().getInt("bookingid"));
        }
    }

    @When("I retrieve the created booking")
    public void retrieveCreatedBooking() {
        context.setResponse(bookingClient.getBooking(context.getBookingId()));
    }

    @When("I fully update the booking with valid authentication")
    public void fullyUpdateBookingWithValidAuthentication() {
        ensureToken();
        context.setResponse(bookingClient.updateBooking(
                context.getBookingId(),
                context.getPayload(),
                context.getToken()
        ));
    }

    @When("I fully update the booking without authentication")
    public void fullyUpdateBookingWithoutAuthentication() {
        context.setResponse(bookingClient.updateBookingWithoutAuth(
                context.getBookingId(),
                context.getPayload()
        ));
    }

    @When("I partially update the booking with valid authentication")
    public void partiallyUpdateBookingWithValidAuthentication() {
        ensureToken();
        context.setResponse(bookingClient.patchBooking(
                context.getBookingId(),
                context.getPayload(),
                context.getToken()
        ));
    }

    @When("I partially update the booking with token {string}")
    public void partiallyUpdateBookingWithToken(String token) {
        context.setResponse(bookingClient.patchBooking(
                context.getBookingId(),
                context.getPayload(),
                token
        ));
    }

    @When("I delete the booking with valid authentication")
    public void deleteBookingWithValidAuthentication() {
        ensureToken();
        context.setResponse(bookingClient.deleteBooking(context.getBookingId(), context.getToken()));
    }

    @When("I delete the booking without authentication")
    public void deleteBookingWithoutAuthentication() {
        context.setResponse(bookingClient.deleteBookingWithoutAuth(context.getBookingId()));
    }

    private void ensureToken() {
        if (context.getToken() == null || context.getToken().isBlank()) {
            context.setResponse(authClient.createToken(
                    ConfigReader.get("auth.username"),
                    ConfigReader.get("auth.password")
            ));
            context.setToken(context.getResponse().jsonPath().getString("token"));
        }
    }
}
