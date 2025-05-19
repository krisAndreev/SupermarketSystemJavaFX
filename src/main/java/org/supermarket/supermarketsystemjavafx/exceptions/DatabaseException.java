package org.supermarket.supermarketsystemjavafx.exceptions;

public class DatabaseException extends Exception {
    private final String operation;

    public DatabaseException(String operation, String message) {
        super(String.format("Database operation '%s' failed: %s", operation, message));
        this.operation = operation;
    }

    public DatabaseException(String operation, Throwable cause) {
        super(String.format("Database operation '%s' failed", operation), cause);
        this.operation = operation;
    }

    // Getter
    public String getOperation() {
        return operation;
    }
}