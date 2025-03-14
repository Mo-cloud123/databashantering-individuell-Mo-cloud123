package se.systementor;

 // Importera rätt anslutningsklass
import se.systementor.models.OrderItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDAO {

    // Metod för att spara en order i databasen
    public void saveOrder(List<OrderItem> items) {
        String sqlOrder = "INSERT INTO orders (OrderDate, TotalAmount) VALUES (NOW(), ?)";
        String sqlDetails = "INSERT INTO orderdetails (OrderId, ProductId, Quantity, UnitPrice, VatRate) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = se.systementor.DB.DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            double totalAmount = items.stream().mapToDouble(OrderItem::getTotal).sum();

            PreparedStatement stmtOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
            stmtOrder.setDouble(1, totalAmount);
            stmtOrder.executeUpdate();

            ResultSet rs = stmtOrder.getGeneratedKeys();
            if (rs.next()) {
                int orderId = rs.getInt(1);

                PreparedStatement stmtDetails = conn.prepareStatement(sqlDetails);
                for (OrderItem item : items) {
                    stmtDetails.setInt(1, orderId);
                    stmtDetails.setInt(2, item.getProduct().getId());
                    stmtDetails.setInt(3, item.getQuantity());
                    stmtDetails.setDouble(4, item.getUnitPrice());
                    stmtDetails.setDouble(5, item.getVatRate());
                    stmtDetails.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String generateReceiptNumber() {
        return String.format("%d-%s",
                System.currentTimeMillis(),
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
        );
    }
}
