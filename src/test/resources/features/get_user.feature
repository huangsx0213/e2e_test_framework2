Feature: Get User

  Scenario: Retrieve the created user using stored user ID
    Given I am using the "dev" environment
    And I am working on the "project1" project
    And I have a "GET" request to "get_user"
    And I use the stored value "user_id" as "id" in the request
    When I send the request
    Then the response status code should be 200
    And the response should contain:
      | id       | ${user_id} |
      | username | testuser   |