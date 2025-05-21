package org.supermarket.supermarketsystemjavafx.models;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Product {
    private final IntegerProperty id;
    private final StringProperty name;
    private final DoubleProperty purchasePrice;
    private final DoubleProperty sellingPrice;
    private final ObjectProperty<Category> category;
    private final ObjectProperty<LocalDate> expirationDate;
    private final IntegerProperty quantity;
    private final StringProperty barcode;
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public enum Category { FOOD, NON_FOOD }

    public Product(int id, String name, double purchasePrice, double sellingPrice,
                   Category category, LocalDate expirationDate, int quantity, String barcode) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.purchasePrice = new SimpleDoubleProperty(purchasePrice);
        this.sellingPrice = new SimpleDoubleProperty(sellingPrice);
        this.category = new SimpleObjectProperty<>(category);
        this.expirationDate = new SimpleObjectProperty<>(expirationDate);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.barcode = new SimpleStringProperty(barcode);
    }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public DoubleProperty purchasePriceProperty() { return purchasePrice; }
    public DoubleProperty sellingPriceProperty() { return sellingPrice; }
    public ObjectProperty<Category> categoryProperty() { return category; }
    public ObjectProperty<LocalDate> expirationDateProperty() { return expirationDate; }
    public IntegerProperty quantityProperty() { return quantity; }
    public StringProperty barcodeProperty() { return barcode; }
    public BooleanProperty selectedProperty() { return selected; }

    // Regular getters
    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public double getPurchasePrice() { return purchasePrice.get(); }
    public double getSellingPrice() { return sellingPrice.get(); }
    public Category getCategory() { return category.get(); }
    public LocalDate getExpirationDate() { return expirationDate.get(); }
    public int getQuantity() { return quantity.get(); }
    public String getBarcode() { return barcode.get(); }
    public boolean isSelected() { return selected.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setName(String name) { this.name.set(name); }
    public void setPurchasePrice(double price) { this.purchasePrice.set(price); }
    public void setSellingPrice(double price) { this.sellingPrice.set(price); }
    public void setCategory(Category category) { this.category.set(category); }
    public void setExpirationDate(LocalDate date) { this.expirationDate.set(date); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public void setBarcode(String barcode) { this.barcode.set(barcode); }
    public void setSelected(boolean selected) { this.selected.set(selected); }
}