package se.systementor.models;

public class OrderItem {
    private Product product;
    private int quantity;

    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public double getTotal() {
        return product.getPrice() * quantity;
    }

    public double getVatAmount() {
        return (product.getPrice() * product.getVatRate() / 100) * quantity;
    }

    public int getProductId() {
        return product.getId();
    }

    public double getUnitPrice() {
        return product.getPrice();
    }

    public double getVatRate() {
        return product.getVatRate();
    }

    @Override
    public String toString() {
        return String.format("%dx %s\t%.2f kr", 
            quantity, 
            product.getName(), 
            getTotal());
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }
}
