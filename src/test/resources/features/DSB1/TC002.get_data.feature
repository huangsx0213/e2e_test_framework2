@api
Feature: TC002 Get Data by Filters

  Background:
    Given I am working on the "DSB1" project
    And I am using the "dev" environment


  @get-data @negative
  Scenario: Successfully Get Data by Filters
    Given I have test cases for "TC001"
    When I execute the API request
    Then I verify the API response
    And I store the response value