package utils;

import models.Booking;
import models.BookingDates;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TestDataFactory {
    private TestDataFactory() {
    }

    public static Booking validBooking() {
        String uniqueValue = String.valueOf(System.currentTimeMillis());
        return new Booking(
                "Auto" + uniqueValue,
                "Tester",
                175,
                true,
                new BookingDates(LocalDate.now().plusDays(7).toString(), LocalDate.now().plusDays(10).toString()),
                "Breakfast"
        );
    }

    public static Booking updatedBooking() {
        String uniqueValue = String.valueOf(System.currentTimeMillis());
        return new Booking(
                "Updated" + uniqueValue,
                "Guest",
                225,
                false,
                new BookingDates(LocalDate.now().plusDays(14).toString(), LocalDate.now().plusDays(17).toString()),
                "Late checkout"
        );
    }

    public static Map<String, Object> partialUpdate() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("firstname", "Patched" + System.currentTimeMillis());
        payload.put("additionalneeds", "Dinner");
        return payload;
    }

    public static Object invalidBookingPayload(String invalidCase) {
        Map<String, Object> payload = validBookingAsMap();

        switch (invalidCase.toLowerCase()) {
            case "missing firstname" -> payload.remove("firstname");
            case "missing lastname" -> payload.remove("lastname");
            case "missing bookingdates" -> payload.remove("bookingdates");
            case "negative total price" -> payload.put("totalprice", -100);
            case "zero total price" -> payload.put("totalprice", 0);
            case "total price as text" -> payload.put("totalprice", "one hundred");
            case "checkout before checkin" -> payload.put("bookingdates", Map.of(
                    "checkin", LocalDate.now().plusDays(5).toString(),
                    "checkout", LocalDate.now().plusDays(2).toString()
            ));
            case "malformed checkin date" -> payload.put("bookingdates", Map.of(
                    "checkin", "not-a-date",
                    "checkout", LocalDate.now().plusDays(2).toString()
            ));
            case "empty payload" -> {
                return new HashMap<String, Object>();
            }
            default -> throw new IllegalArgumentException("Unsupported invalid booking case: " + invalidCase);
        }

        return payload;
    }

    private static Map<String, Object> validBookingAsMap() {
        Booking booking = validBooking();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("firstname", booking.getFirstname());
        payload.put("lastname", booking.getLastname());
        payload.put("totalprice", booking.getTotalprice());
        payload.put("depositpaid", booking.isDepositpaid());
        payload.put("bookingdates", Map.of(
                "checkin", booking.getBookingdates().getCheckin(),
                "checkout", booking.getBookingdates().getCheckout()
        ));
        payload.put("additionalneeds", booking.getAdditionalneeds());
        return payload;
    }
}
