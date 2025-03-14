package se.systementor.DB;

import se.systementor.models.OrderItem;
import se.systementor.models.Product;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    // Hämtar alla produkter från databasen och returnerar en lista av produkter
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>(); // Skapar en lista för att lagra produkter
        String sql = "SELECT ProductId, Name, Price, VatRate, CategoryId FROM products";
        // SQL-fråga för att hämta produkter

        try (Connection conn = se.systementor.DB.DatabaseConnection.getConnection(); // Skapar en anslutning till databasen
             Statement stmt = conn.createStatement(); // Skapar ett statement för att köra SQL
             ResultSet rs = stmt.executeQuery(sql)) { // Kör SQL-frågan och får resultat

            while (rs.next()) { // Loopar igenom varje rad i resultatet
                Product product = new Product(
                        rs.getInt("ProductId"), // Hämtar produktens ID
                        rs.getString("Name"), // Hämtar produktens namn
                        rs.getDouble("Price"), // Hämtar priset på produkten
                        rs.getDouble("VatRate"), // Hämtar momssatsen
                        rs.getInt("CategoryId")
                );
                products.add(product); // Lägger till produkten i listan
            }
        } catch (SQLException e) { // Om något går fel med databasen
            e.printStackTrace(); // Skriver ut felmeddelandet
        }
        return products; // Returnerar listan med produkter
    }

    // Sparar en order i databasen
    public int saveOrder(List<OrderItem> orderItems, String receiptNumber, double total, double totalVat, LocalDateTime orderTime) {
        // Spara ordern i databasen inklusive orderTime

    // Kod för att spara ordern


        int orderId = 0;


        try (Connection conn = se.systementor.DB.DatabaseConnection.getConnection()) {
            // Spara ordern i orders-tabellen
            String orderQuery = "INSERT INTO orders (OrderDate, TotalAmount, ReceiptNumber) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS)) {
                ps.setTimestamp(1, Timestamp.valueOf(orderTime));
                ps.setDouble(2, total);
                ps.setString(3, receiptNumber);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                    }
                }
            }

            // Spara varje orderrad i orderdetails-tabellen
            String detailsQuery = "INSERT INTO orderdetails (OrderId, ProductId, Quantity, UnitPrice, VatRate, ReceiptNumber, TotalPrice, OrderDateTime) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(detailsQuery)) {
                for (OrderItem item : orderItems) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, item.getProduct().getId());
                    ps.setInt(3, item.getQuantity());
                    ps.setDouble(4, item.getProduct().getPrice());
                    ps.setDouble(5, item.getProduct().getVatRate());
                    ps.setString(6, receiptNumber);
                    ps.setDouble(7, item.getTotal());
                    ps.setTimestamp(8, Timestamp.valueOf(orderTime));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderId;
    }


}