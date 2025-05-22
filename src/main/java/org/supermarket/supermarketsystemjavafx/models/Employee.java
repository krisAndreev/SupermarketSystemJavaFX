package org.supermarket.supermarketsystemjavafx.models;

import javafx.beans.property.*;

public class Employee {
    private final IntegerProperty id;
    private final StringProperty name;
    private final DoubleProperty monthlySalary;
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public Employee(int id, String name, double monthlySalary) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.monthlySalary = new SimpleDoubleProperty(monthlySalary);
    }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public DoubleProperty monthlySalaryProperty() { return monthlySalary; }
    public BooleanProperty selectedProperty() { return selected; }

    // Regular getters
    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public double getMonthlySalary() { return monthlySalary.get(); }
    public boolean isSelected() { return selected.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setName(String name) { this.name.set(name); }
    public void setMonthlySalary(double salary) { this.monthlySalary.set(salary); }
    public void setSelected(boolean selected) { this.selected.set(selected); }
}