Feature: Create User

  Scenario: Create a new user and store the user ID
    Given I am using the "dev" environment
    And I am working on the "project1" project
    And I have a "POST" request to "create_user"
    And I set the request body using template "create_user" with:
      | username | testuser |
      | email    | testuser@example.com |
      | password | password123 |
    When I send the request
    Then the response status code shoul
    And I store the response value "id" as "user_id"