# Reservation Hub API Automation Assignment

This repository contains an API regression suite for the public Restful Booker service:

```text
https://restful-booker.herokuapp.com
```

The suite is built with **Java, Maven, RestAssured, and Cucumber**, producing HTML/JSON/XML reports plus Maven Surefire output. The test flow is: feature file -> step definition -> reusable client/service layer -> RestAssured request -> shared scenario context -> assertions and reports.

## Test Strategy

The suite focuses on the incident described in the assignment: invalid booking data, such as a negative price or checkout before checkin, should not reach production silently. It also covers the core booking lifecycle so regressions in create, read, update, and cancel flows are caught quickly.

Covered areas:

- Health check using `GET /ping`.
- Authentication using `POST /auth`.
- Booking lifecycle: create, retrieve, full update, partial update, and delete.
- Authorization checks for protected write operations.
- Negative and boundary validation for missing fields, empty payloads, wrong data types, negative or zero prices, malformed dates, and checkout before checkin.
- Response contract checks for booking objects and booking ID lists.
- Query filters on `GET /booking` (list booking IDs): firstname/lastname filters are covered as passing checks, and checkin/checkout filters are covered as a high-risk check that currently exposes a documented API defect.

Deliberately left out:

- Database validation, because this is a public black-box API with no DB access.
- SOAP/WSDL coverage, because all endpoints in scope are REST endpoints.
- Full performance testing, because the assignment asks for regression safety rather than load testing.
- Tests that depend on preloaded records, because the shared instance resets roughly every 10 minutes.

## Project Structure

```text
src/test/java
  clients      Reusable RestAssured API client classes
  context      Shared scenario state: token, booking id, payload, response
  models       Booking request and response POJOs
  runners      Cucumber JUnit Platform runner
  steps        Cucumber step definitions kept thin
  utils        Config reader, schema validator, test data, report attachments

src/test/resources
  features     BDD Cucumber feature files grouped by API risk area
  schemas      JSON schema files for response contract checks
  config.properties
  junit-platform.properties  Cucumber glue/plugin config (report output paths)

reports
  cucumber-reports
    cucumber.html
    cucumber.json
    cucumber.xml
```

Report outputs:

```text
reports/cucumber-reports/cucumber.html
reports/cucumber-reports/cucumber.json
reports/cucumber-reports/cucumber.xml
target/surefire-reports/
```

The `reports/cucumber-reports` folder is updated on every test run, so the checked-in report stays aligned with the latest execution for assignment submission.

## Running The Tests

Prerequisites:

- Java 17
- Maven 3.9 or later

Run the full suite:

```bash
mvn clean test
```

Current expected result against the public API:

```text
26 scenarios run: 16 pass, 10 fail
```

The failing scenarios are the high-risk negative booking validation checks, plus one high-risk booking-search check. They assert the behavior a production booking API should have, while the current public API accepts invalid data, returns `500` instead of a clean validation error, or (for the date filter) fails to return the booking that matches the requested dates. Those failures are documented in `BUGS.md`.

Run only the currently passing functional, auth, contract, and not-found checks:

```bash
mvn clean test -Dcucumber.filter.tags="not @high-risk"
```

On Windows PowerShell, the tag expression must be single-quoted (double quotes are consumed by PowerShell before Maven sees them):

```powershell
mvn clean test '-Dcucumber.filter.tags=not @high-risk'
```

Open the generated HTML report:

```text
reports/cucumber-reports/cucumber.html
```

Override configuration with system properties when needed:

```bash
mvn clean test -Dbase.url=https://restful-booker.herokuapp.com
```

## Reporting

The Cucumber reports include:

- Overall pass/fail summary.
- Per-scenario results grouped by feature and tag.
- Assertion failures for failed negative tests.
- Embedded HTTP request and response details for each API call.
- JSON and JUnit XML outputs that can be consumed by CI tools or reporting integrations.

Cucumber HTML is for human review, Cucumber JSON is for downstream processing, and JUnit XML/Surefire is for build visibility — giving clean artifacts that can be attached to a submission.

## Handling The Shared Environment

The Restful Booker instance is shared and resets periodically, so the tests are designed to be independent:

- Each scenario that needs a booking creates its own booking first.
- Tests do not depend on existing IDs or execution order.
- Generated first names make test data easy to identify and reduce collisions.
- The only retry is around `/ping` to tolerate a cold start.
- Validation failures are not retried because they may represent real API defects.

## Known Limitations

- Some high-risk tests are expected to fail because the public API currently accepts invalid booking data, returns `500` for validation problems, or (for the date-filter search case) fails to return the booking that matches the requested dates — every distinct failure behavior observed is documented as its own entry in `BUGS.md` (BUG-001 through BUG-007).
- The API documentation (`apidoc/index.html`) only describes fields and example payloads in prose/HTML — it does not publish a formal, machine-readable contract (no OpenAPI/Swagger spec). So response contract checks use hand-authored JSON Schema files, written by inspecting the documented fields and example responses, for the important response shapes.
- No booking created by a test is explicitly deleted, except in the scenario whose purpose is to test deletion itself. The suite relies entirely on the API's own ~10-minute reset to clear test data rather than performing its own teardown.
- Schema/contract validation only covers success-path response shapes (`booking-schema.json`, `booking-created-schema.json`). Error responses (400/403/404/500) are asserted by status code only, not by response shape.
- CI-only integrations (e.g. test-management upload, build-summary notifications) are not included because this assignment is delivered as a standalone repository rather than a CI pipeline.
