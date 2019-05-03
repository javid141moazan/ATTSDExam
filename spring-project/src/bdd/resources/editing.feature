Feature: Restaurants editing

Scenario: Add new restaurant
	Given The User is on Home Page
	When The User navigates to "save" page
	And Enters restaurant name "new restaurant" and average price "10" and press click
	Then The User is redirected to Home Page
	And A table must show the added restaurant with name "new restaurant", average price "10" and id is positive

Scenario: Edit a non existing restaurant
	Given The User is on Home Page
	When The User navigates to "edit" page with id "-1"
	Then A message "No restaurant found with id: " + "-1" must be shown