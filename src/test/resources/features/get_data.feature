@api @data-management
Feature: User Management API

  Background:
    Given I am using the "dev" environment
    And I am working on the "DSB1" project

  @get-data @positive
  Scenario: Successfully create a new user
    Given I have test cases for "TC001"
    When I execute the API request
    Then I verify the API response
    And I store the response value

#  @create-user @negative
#  Scenario: Attempt to create a user with invalid data
#    Given I have test data for "CREATE_USER_002"
#    When I execute the API request
#    Then I verify the API response
#
#  @get-user @positive
#  Scenario: Retrieve an existing user
#    Given I have test data for "GET_USER_001"
#    And I use the stored value "user_id" as a path parameter
#    When I execute the API request
#    Then I verify the API response
#
#  @get-user @negative
#  Scenario: Attempt to retrieve a non-existent user
#    Given I have test data for "GET_USER_002"
#    When I execute the API request
#    Then I verify the API response
#
#  @update-user @positive
#  Scenario: Successfully update an existing user
#    Given I have test data for "UPDATE_USER_001"
#    And I use the stored value "user_id" as a path parameter
#    When I execute the API request
#    Then I verify the API response
#
#  @delete-user @positive
#  Scenario: Successfully delete an existing user
#    Given I have test data for "DELETE_USER_001"
#    And I use the stored value "user_id" as a path parameter
#    When I execute the API request
#    Then I verify the API response
#
#  @user-count @dynamic-validation
#  Scenario: Verify user count increases after creating a new user
#    Given I have test data for "CREATE_USER_003"
#    And I set up dynamic validation with reference API endpoint "user_count"
#    When I send the request with dynamic validation expecting:
#      | total_users | +1 |
#    Then I verify the API response
#
#  @user-login @security
#  Scenario Outline: Verify user login with different credentials
#    Given I have test data for "<test_case_id>"
#    When I execute the API request
#    Then I verify the API response
#
#    Examples:
#      | test_case_id     |
#      | USER_LOGIN_001   |
#      | USER_LOGIN_002   |
#      | USER_LOGIN_003   |
#
#  @user-search @performance
#  Scenario: Search users with pagination and verify response time
#    Given I have test data for "SEARCH_USERS_001"
#    And I set the performance threshold to 500 milliseconds
#    When I execute the API request
#    Then I verify the API response
#    And I verify the response time is within the threshold
#
#  @user-bulk-operations
#  Scenario: Perform bulk user creation and verify all users are created
#    Given I have test data for "BULK_CREATE_USERS_001"
#    When I execute the API request
#    Then I verify the API response
#    And I verify all users in the bulk request are created successfully