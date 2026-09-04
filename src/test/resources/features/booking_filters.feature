@booking @filter
Feature: Booking API list filters

  Background:
    Given I have created a booking

  @contract
  Scenario: Filter booking ids by firstname and lastname
    When I list booking ids filtered by the created booking's firstname and lastname
    Then the response status should be 200
    And the filtered booking id list should include the created booking id

  @high-risk
  Scenario: Filter booking ids by checkin and checkout dates
    When I retrieve the created booking
    Then the response status should be 200
    And the booking details should match the current booking payload
    When I list booking ids filtered by the created booking's checkin and checkout dates
    Then the response status should be 200
    And the filtered booking id list should include the created booking id

  @boundary
  Scenario: Filter booking ids by a name that does not exist returns no results
    When I list booking ids filtered by firstname "NoSuchGuest9999XYZ" and lastname "NoSuchGuest9999XYZ"
    Then the response status should be 200
    And the filtered booking id list should be empty
