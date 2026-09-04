@booking @negative
Feature: Booking API negative and boundary cases

  @high-risk
  Scenario Outline: Reject invalid booking payloads
    Given I have an invalid booking payload for "<case>"
    When I create the booking
    Then the response status should be a client error

    Examples:
      | case                       |
      | missing firstname          |
      | missing lastname           |
      | missing bookingdates       |
      | negative total price       |
      | zero total price           |
      | total price as text        |
      | checkout before checkin    |
      | malformed checkin date     |
      | empty payload              |

  @not-found
  Scenario: Return not found for a booking id that does not exist
    Given I use booking id 999999999
    When I retrieve the created booking
    Then the response status should be 404

  @not-found
  Scenario: Reject full update for a booking id that does not exist
    Given I use booking id 999999999
    And I have a valid updated booking payload
    When I fully update the booking with valid authentication
    Then the response status should be 405

  @not-found
  Scenario: Reject deletion for a booking id that does not exist
    Given I use booking id 999999999
    When I delete the booking with valid authentication
    Then the response status should be 405
