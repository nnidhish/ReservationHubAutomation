package steps;

import context.TestContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import models.Booking;
import utils.SchemaValidator;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonAssertionSteps {
    private final TestContext context;

    public CommonAssertionSteps(TestContext context) {
        this.context = context;
    }

    @Then("the response status should be {int}")
    public void responseStatusShouldBe(int expectedStatusCode) {
        assertThat(context.getResponse().statusCode()).isEqualTo(expectedStatusCode);
    }

    @Then("the response status should be a client error")
    public void responseStatusShouldBeClientError() {
        assertThat(context.getResponse().statusCode())
                .as("Invalid input should be rejected with a 4xx status code")
                .isBetween(400, 499);
    }

    @And("the booking should be created successfully")
    public void bookingShouldBeCreatedSuccessfully() {
        assertThat(context.getBookingId()).isPositive();
        assertThat(context.getResponse().jsonPath().getMap("booking")).isNotEmpty();
        assertBookingMatches("booking", context.getCurrentBooking());
    }

    @And("the booking details should match the current booking payload")
    public void bookingDetailsShouldMatchCurrentPayload() {
        assertBookingMatches("", context.getCurrentBooking());
    }

    @And("the partial update should be reflected in the response")
    public void partialUpdateShouldBeReflected() {
        Map<String, Object> partialUpdate = context.getPartialUpdate();
        partialUpdate.forEach((field, expectedValue) ->
                assertThat(context.getResponse().jsonPath().getString(field)).isEqualTo(String.valueOf(expectedValue))
        );
    }

    @And("the created booking response should match the booking schema")
    public void createdBookingResponseShouldMatchBookingSchema() {
        SchemaValidator.validateCreatedBooking(context.getResponse());
    }

    @And("the booking response should match the booking schema")
    public void bookingResponseShouldMatchBookingSchema() {
        SchemaValidator.validateBooking(context.getResponse());
    }

    @And("the booking id list should use the expected contract")
    public void bookingIdListShouldUseExpectedContract() {
        List<Map<String, Object>> bookings = context.getResponse().jsonPath().getList("$");
        assertThat(bookings).isNotNull();
        assertThat(bookings).allSatisfy(bookingId ->
                assertThat(bookingId).containsKey("bookingid")
        );
    }

    @And("the filtered booking id list should include the created booking id")
    public void filteredBookingIdListShouldIncludeCreatedBookingId() {
        List<Integer> ids = context.getResponse().jsonPath().getList("bookingid", Integer.class);
        assertThat(ids).contains(context.getBookingId());
    }

    @And("the filtered booking id list should be empty")
    public void filteredBookingIdListShouldBeEmpty() {
        List<Map<String, Object>> bookings = context.getResponse().jsonPath().getList("$");
        assertThat(bookings).isEmpty();
    }

    private void assertBookingMatches(String prefix, Booking expected) {
        String pathPrefix = prefix == null || prefix.isBlank() ? "" : prefix + ".";
        assertThat(context.getResponse().jsonPath().getString(pathPrefix + "firstname")).isEqualTo(expected.getFirstname());
        assertThat(context.getResponse().jsonPath().getString(pathPrefix + "lastname")).isEqualTo(expected.getLastname());
        assertThat(context.getResponse().jsonPath().getInt(pathPrefix + "totalprice")).isEqualTo(expected.getTotalprice());
        assertThat(context.getResponse().jsonPath().getBoolean(pathPrefix + "depositpaid")).isEqualTo(expected.isDepositpaid());
        assertThat(context.getResponse().jsonPath().getString(pathPrefix + "bookingdates.checkin"))
                .isEqualTo(expected.getBookingdates().getCheckin());
        assertThat(context.getResponse().jsonPath().getString(pathPrefix + "bookingdates.checkout"))
                .isEqualTo(expected.getBookingdates().getCheckout());
        assertThat(context.getResponse().jsonPath().getString(pathPrefix + "additionalneeds"))
                .isEqualTo(expected.getAdditionalneeds());
    }
}
