package org.supermarket.supermarketsystemjavafx.exceptions;

/**
 * Custom exception thrown when an employee cannot be found in the system
 */
public class EmployeeNotFoundException extends Exception {
    private final int employeeId;
    private final String searchCriteria;

    /**
     * Constructor for when searching by employee ID
     */
    public EmployeeNotFoundException(int employeeId) {
        super(String.format("Employee with ID %d not found", employeeId));
        this.employeeId = employeeId;
        this.searchCriteria = "ID: " + employeeId;
    }

    /**
     * Constructor for when searching by other criteria (e.g., name/PIN)
     */
    public EmployeeNotFoundException(String message) {
        super(message);
        this.employeeId = -1;
        this.searchCriteria = message;
    }

    /**
     * Constructor for when authentication fails
     */
    public EmployeeNotFoundException(String name, String pin) {
        super(String.format("No employee found with name '%s' and provided PIN", name));
        this.employeeId = -1;
        this.searchCriteria = "Name: " + name;
    }

    /**
     * @return The employee ID that was searched for (-1 if search wasn't by ID)
     */
    public int getEmployeeId() {
        return employeeId;
    }

    /**
     * @return The criteria used for the failed search
     */
    public String getSearchCriteria() {
        return searchCriteria;
    }

    /**
     * Helper method to check if this was an authentication failure
     */
    public boolean isAuthenticationFailure() {
        return employeeId == -1 && getMessage().contains("PIN");
    }
}