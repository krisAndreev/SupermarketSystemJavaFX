package org.supermarket.supermarketsystemjavafx.dao;

import org.supermarket.supermarketsystemjavafx.exceptions.DatabaseException;
import org.supermarket.supermarketsystemjavafx.exceptions.EmployeeNotFoundException;
import org.supermarket.supermarketsystemjavafx.models.Employee;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {
    private static final String DB_URL = "jdbc:sqlite:supermarket.db";

    // SQL Statements
    private static final String GET_ALL_EMPLOYEES = "SELECT id, name, monthly_salary FROM employees";
    private static final String GET_EMPLOYEE_BY_ID = "SELECT id, name, monthly_salary FROM employees WHERE id = ?";
    private static final String GET_EMPLOYEE_PIN = "SELECT pin_code FROM employees WHERE id = ?";
    private static final String INSERT_EMPLOYEE = "INSERT INTO employees(name, monthly_salary, pin_code) VALUES(?,?,?)";
    private static final String UPDATE_EMPLOYEE = "UPDATE employees SET name = ?, monthly_salary = ? WHERE id = ?";
    private static final String UPDATE_EMPLOYEE_PIN = "UPDATE employees SET pin_code = ? WHERE id = ?";
    private static final String DELETE_EMPLOYEE = "DELETE FROM employees WHERE id = ?";
    private static final String AUTHENTICATE_EMPLOYEE = "SELECT id FROM employees WHERE name = ? AND pin_code = ?";

    /**
     * Gets all employees from the database
     */
    public static List<Employee> getAllEmployees() throws DatabaseException {
        List<Employee> employees = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(GET_ALL_EMPLOYEES)) {

            while (rs.next()) {
                employees.add(mapResultSetToEmployee(rs));
            }
            return employees;

        } catch (SQLException e) {
            throw new DatabaseException("Get all employees", e);
        }
    }

    /**
     * Gets an employee by ID
     */
    public static Employee getEmployeeById(int id) throws EmployeeNotFoundException, DatabaseException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(GET_EMPLOYEE_BY_ID)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEmployee(rs);
            } else {
                throw new EmployeeNotFoundException(id);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Get employee by ID", e);
        }
    }

    /**
     * Adds a new employee to the database with PIN code
     */
    public static void addEmployee(Employee employee, String pinCode) throws DatabaseException {
        if (pinCode == null || pinCode.length() != 4 || !pinCode.matches("\\d+")) {
            throw new IllegalArgumentException("PIN code must be 4 digits");
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(INSERT_EMPLOYEE, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, employee.getName());
            pstmt.setDouble(2, employee.getMonthlySalary());
            pstmt.setString(3, pinCode);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Add employee", "No rows affected");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    employee.setId(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Add employee", e);
        }
    }

    /**
     * Updates employee information (excluding PIN)
     */
    public static void updateEmployee(Employee employee) throws DatabaseException, EmployeeNotFoundException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_EMPLOYEE)) {

            pstmt.setString(1, employee.getName());
            pstmt.setDouble(2, employee.getMonthlySalary());
            pstmt.setInt(3, employee.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new EmployeeNotFoundException(employee.getId());
            }

        } catch (SQLException e) {
            throw new DatabaseException("Update employee", e);
        }
    }

    /**
     * Updates employee PIN code
     */
    public static void updateEmployeePin(int employeeId, String newPin)
            throws DatabaseException, EmployeeNotFoundException {
        if (newPin == null || newPin.length() != 4 || !newPin.matches("\\d+")) {
            throw new IllegalArgumentException("PIN code must be 4 digits");
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_EMPLOYEE_PIN)) {

            pstmt.setString(1, newPin);
            pstmt.setInt(2, employeeId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new EmployeeNotFoundException(employeeId);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Update employee PIN", e);
        }
    }

    /**
     * Deletes an employee from the database
     */
    public static void deleteEmployee(int employeeId) throws DatabaseException, EmployeeNotFoundException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(DELETE_EMPLOYEE)) {

            pstmt.setInt(1, employeeId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new EmployeeNotFoundException(employeeId);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Delete employee", e);
        }
    }

    /**
     * Authenticates an employee using name and PIN
     * @return Employee ID if authentication succeeds
     */
    public static int authenticateEmployee(String name, String pin)
            throws DatabaseException, EmployeeNotFoundException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(AUTHENTICATE_EMPLOYEE)) {

            pstmt.setString(1, name);
            pstmt.setString(2, pin);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new EmployeeNotFoundException("Invalid credentials");
            }

        } catch (SQLException e) {
            throw new DatabaseException("Authenticate employee", e);
        }
    }

    /**
     * Gets an employee's PIN code (for verification purposes)
     */
    public static String getEmployeePin(int employeeId) throws EmployeeNotFoundException, DatabaseException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(GET_EMPLOYEE_PIN)) {

            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("pin_code");
            } else {
                throw new EmployeeNotFoundException(employeeId);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Get employee PIN", e);
        }
    }

    // Helper method to map ResultSet to Employee object
    private static Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("monthly_salary")
        );
    }
}