@booking
Feature: Booking API core journeys

  @smoke
  Scenario: Health check confirms the API is available
    When I check the API health
    Then the response status should be 201

  @smoke @crud
  Scenario: Create and retrieve a booking
    Given I have a valid booking payload
    When I create the booking
    Then the response status should be 200
    And the booking should be created successfully
    And the created booking response should match the booking schema
    When I retrieve the created booking
    Then the response status should be 200
    And the booking details should match the current booking payload
    And the booking response should match the booking schema

  @crud
  Scenario: Fully update a booking
    Given I have created a booking
    And I have a valid updated booking payload
    When I fully update the booking with valid authentication
    Then the response status should be 200
    And the booking details should match the current booking payload
    When I retrieve the created booking
    Then the booking details should match the current booking payload

  @crud
  Scenario: Partially update a booking
    Given I have created a booking
    And I have a valid partial booking update
    When I partially update the booking with valid authentication
    Then the response status should be 200
    And the partial update should be reflected in the response

  @crud
  Scenario: Delete a booking
    Given I have created a booking
    When I delete the booking with valid authentication
    Then the response status should be 201
    When I retrieve the created booking
    Then the response status should be 404

  @contract
  Scenario: List booking IDs
    When I list booking ids
    Then the response status should be 200
    And the booking id list should use the expected contract
