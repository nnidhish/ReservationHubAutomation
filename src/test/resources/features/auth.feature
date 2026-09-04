@auth
Feature: Authentication and authorization

  @smoke
  Scenario: Generate an authentication token with valid credentials
    When I request an auth token with valid credentials
    Then the response status should be 200
    And the response should contain an auth token

  @negative
  Scenario: Reject invalid credentials
    When I request an auth token with username "admin" and password "wrong-password"
    Then the response status should be 200
    And the auth token should not be created

  @negative @authorization
  Scenario: Reject full booking update without authentication
    Given I have created a booking for authorization checks
    And I have a valid updated booking payload
    When I fully update the booking without authentication
    Then the response status should be 403

  @negative @authorization
  Scenario: Reject partial booking update with an invalid token
    Given I have created a booking for authorization checks
    And I have a valid partial booking update
    When I partially update the booking with token "invalid-token"
    Then the response status should be 403

  @negative @authorization
  Scenario: Reject booking deletion without authentication
    Given I have created a booking for authorization checks
    When I delete the booking without authentication
    Then the response status should be 403
