@api @data-management
Feature: User Management API

  Background:
    Given I am working on the "DSB1" project
    And I am using the "dev" environment


  @get-data @positive
  Scenario: Successfully create a new user
    Given I have test cases for "TC001"
    When I execute the API request
    Then I verify the API response
    And I store the response value
