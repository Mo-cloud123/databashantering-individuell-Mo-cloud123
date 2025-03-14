package se.systementor;

import se.systementor.DB.DatabaseConnection;
import se.systementor.models.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    // Hämtar alla produkter från databasen
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT ProductId, Name, Price, VatRate, CategoryId FROM products";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("ProductId"),
                        rs.getString("Name"),
                        rs.getDouble("Price"),
                        rs.getDouble("VatRate"),
                        rs.getInt("CategoryId")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }


    // Hämtar nästa kvittonummer
    public int getNextReceiptNumber() {
        String sql = "SELECT MAX(ReceiptNumber) AS MaxReceipt FROM orders";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if(rs.next()) {
                return rs.getInt("MaxReceipt") + 1;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching next receipt number: " + e.getMessage());
            e.printStackTrace();
        }
        return 1; // Börja med kvittonummer 1 om inget hittas
    }

}