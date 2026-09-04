# Bug Report

The defects below are written from the point of view of a production Reservation Hub API. They are intentionally captured by the negative scenarios in this suite.

## BUG-001: Booking can be created with a negative total price

Severity: High. A negative booking price can create financial and reconciliation issues for partners and customers.

Steps to reproduce:

```bash
curl -i -X POST "https://restful-booker.herokuapp.com/booking" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "firstname": "Invalid",
    "lastname": "Price",
    "totalprice": -100,
    "depositpaid": true,
    "bookingdates": {
      "checkin": "2026-09-10",
      "checkout": "2026-09-12"
    },
    "additionalneeds": "Breakfast"
  }'
```

Expected behavior:

The API should reject the request with a 4xx response and a useful validation message.

Actual behavior:

The API accepts the request and creates a booking.

Product impact:

This is the same class of incident described in the assignment. It allows invalid commercial data into the booking system.

## BUG-002: Booking can be created with checkout before checkin

Severity: High. An impossible stay date range can break partner availability, billing, and customer communication.

Steps to reproduce:

```bash
curl -i -X POST "https://restful-booker.herokuapp.com/booking" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "firstname": "Invalid",
    "lastname": "Dates",
    "totalprice": 150,
    "depositpaid": true,
    "bookingdates": {
      "checkin": "2026-09-15",
      "checkout": "2026-09-12"
    },
    "additionalneeds": "Breakfast"
  }'
```

Expected behavior:

The API should reject the booking because checkout must be after checkin.

Actual behavior:

The API accepts the request and creates a booking.

Product impact:

Partners may receive reservations with impossible stay periods, creating operational and customer support issues.

## BUG-003: Booking can be created with a malformed checkin date

Severity: Medium. Malformed dates reduce data quality and can cause downstream parsing or reporting failures.

Steps to reproduce:

```bash
curl -i -X POST "https://restful-booker.herokuapp.com/booking" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "firstname": "Invalid",
    "lastname": "DateFormat",
    "totalprice": 150,
    "depositpaid": true,
    "bookingdates": {
      "checkin": "not-a-date",
      "checkout": "2026-09-12"
    },
    "additionalneeds": "Breakfast"
  }'
```

Expected behavior:

The API should reject the booking with a 4xx response because `checkin` is not a valid date.

Actual behavior:

The API accepts the request and creates a booking.

Product impact:

Invalid dates can fail later in reporting, partner integrations, or UI rendering even though creation appeared successful.

## BUG-004: Missing required booking fields return 500 instead of validation errors

Severity: Medium. Invalid customer requests should not create server errors because a 500 hides the real validation issue and can trigger false production incident alerts.

Steps to reproduce:

```bash
curl -i -X POST "https://restful-booker.herokuapp.com/booking" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "lastname": "MissingFirstname",
    "totalprice": 150,
    "depositpaid": true,
    "bookingdates": {
      "checkin": "2026-09-10",
      "checkout": "2026-09-12"
    },
    "additionalneeds": "Breakfast"
  }'
```

Expected behavior:

The API should reject the request with a 4xx validation response explaining that `firstname` is required.

Actual behavior:

The API returns a 500 server error.

Product impact:

Bad client input is reported as a server failure, making issues harder for support, QA, and engineering to triage.

## BUG-005: A non-numeric total price is silently stored as null instead of being rejected

Severity: High. This is worse than simply accepting bad data: the price is silently discarded, leaving a booking with no price at all and no error to alert the caller.

Steps to reproduce:

```bash
curl -i -X POST "https://restful-booker.herokuapp.com/booking" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "firstname": "Invalid",
    "lastname": "TextPrice",
    "totalprice": "one hundred",
    "depositpaid": true,
    "bookingdates": {
      "checkin": "2026-09-10",
      "checkout": "2026-09-12"
    },
    "additionalneeds": "Breakfast"
  }'
```

Expected behavior:

The API should reject the request with a 4xx response because `totalprice` must be a number.

Actual behavior:

The API returns `200 OK` and creates the booking, but the response shows `"totalprice": null` — the invalid string value is silently dropped rather than the request being rejected.

Product impact:

This is a more severe variant of the incident described in the assignment: instead of an obviously wrong price making it to production, the price disappears entirely with no signal that anything went wrong, which is harder to detect in monitoring or reconciliation.

## BUG-006: Booking can be created with a total price of zero with no validation signal

Severity: Low. A zero price is not inherently invalid (e.g. a comped stay), but the API applies no rule or flag around it, so it can't be distinguished from a broken upstream price calculation.

Steps to reproduce:

```bash
curl -i -X POST "https://restful-booker.herokuapp.com/booking" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "firstname": "Invalid",
    "lastname": "ZeroPrice",
    "totalprice": 0,
    "depositpaid": true,
    "bookingdates": {
      "checkin": "2026-09-10",
      "checkout": "2026-09-12"
    },
    "additionalneeds": "Breakfast"
  }'
```

Expected behavior:

The API should either explicitly support zero-price bookings (e.g. via a reason/flag) or reject an unqualified zero price so it isn't confused with a calculation failure.

Actual behavior:

The API accepts the request and creates the booking with `"totalprice": 0` and no distinguishing signal.

Product impact:

Without a way to tell "intentional free stay" apart from "price calculation failed upstream," a real pricing defect could go unnoticed until a partner escalation.

## BUG-007: GET /booking checkin/checkout date filters do not return matching bookings

Severity: High. The date-range search is one of the few query capabilities partners have for finding their own bookings; if it silently returns wrong results, partners can miss or misreport reservations with no error to indicate anything is wrong.

Steps to reproduce:

```bash
# Create a booking with a unique, far-future date range
curl -i -X POST "https://restful-booker.herokuapp.com/booking" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "firstname": "UniqueDateTest",
    "lastname": "Guest",
    "totalprice": 175,
    "depositpaid": true,
    "bookingdates": {
      "checkin": "2027-11-03",
      "checkout": "2027-11-09"
    },
    "additionalneeds": "Breakfast"
  }'

# Confirm the booking still exists by retrieving it directly by id
curl -i "https://restful-booker.herokuapp.com/booking/<bookingid-from-create-response>"

# Immediately query using the exact same checkin/checkout values
curl -i "https://restful-booker.herokuapp.com/booking?checkin=2027-11-03&checkout=2027-11-09"
```

Expected behavior:

The response should include the booking id that was just created with those exact dates (per the documented behavior: "Return bookings that have a checkin/checkout date greater than or equal to the set date").

Actual behavior:

The booking is still retrievable directly by id immediately after creation, but the date-filter query does not include that booking. Across repeated checks, the API returned either an empty list (`[]`) or unrelated/stale booking ids for the requested date range.

Product impact:

The date-range filter does not work as documented. Because direct id retrieval still works, this is not explained by the shared API reset removing the record. Any partner or internal tool relying on date filters to search for bookings by stay dates would silently miss valid bookings rather than receiving an error, making the defect hard to notice until a partner escalation (the same class of "silent" incident described in the assignment).
