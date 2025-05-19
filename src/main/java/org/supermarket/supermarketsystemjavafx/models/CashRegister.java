package org.supermarket.supermarketsystemjavafx.models;

import javafx.beans.property.*;

public class CashRegister {
    private final IntegerProperty id;
    private final IntegerProperty currentEmployeeId;

    public CashRegister(int id, int currentEmployeeId) {
        this.id = new SimpleIntegerProperty(id);
        this.currentEmployeeId = new SimpleIntegerProperty(currentEmployeeId);
    }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty currentEmployeeIdProperty() { return currentEmployeeId; }

    // Regular getters
    public int getId() { return id.get(); }
    public int getCurrentEmployeeId() { return currentEmployeeId.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setCurrentEmployeeId(int employeeId) { this.currentEmployeeId.set(employeeId); }
}