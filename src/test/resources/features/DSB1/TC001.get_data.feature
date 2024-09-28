@api
Feature: TC001 Add new item

  Background:
    Given I am working on the "DSB1" project
    And I am using the "sit" environment


  @get-data @positive
  Scenario: Successfully Get Data by Filters
    Given I have test cases for "AddItem01"
    When I execute the API request
    Then I verify the API response
    And I store the response value
