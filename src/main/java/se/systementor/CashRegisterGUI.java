package se.systementor;

import se.systementor.DB.ProductRepository;
import se.systementor.models.OrderItem;
import se.systementor.models.Product;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CashRegisterGUI extends JFrame {
    private JTextArea receiptArea;
    private JTextField quantityField;
    private JPanel productsPanel;
    private List<Product> products;
    private List<OrderItem> currentOrder;
    private ProductRepository repository;
    private int nextReceiptNumber = 1;
    private Product selectedProduct; // Ny variabel för att hålla den valda produkten
    private String currentReceiptNumber; // Håller nuvarande kvittonummer för den aktuella ordern


    public CashRegisterGUI() {
        repository = new ProductRepository();
        currentOrder = new ArrayList<>();

        setTitle("Kassa System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        createProductsPanel();
        createReceiptPanel();
        createControlPanel();

        pack();
        setLocationRelativeTo(null);
    }

    private void createProductsPanel() {
        productsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        productsPanel.setBackground(Color.GREEN);
        products = repository.getAllProducts();

        for (Product product : products) {
            JButton btn = new JButton(product.getName());
            btn.setBackground(Color.WHITE);
            btn.setPreferredSize(new Dimension(100, 30));
            btn.addActionListener(e -> handleProductClick(product));
            productsPanel.add(btn);
        }

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(Color.GREEN);
        topContainer.add(productsPanel, BorderLayout.NORTH);

        add(topContainer, BorderLayout.CENTER);
    }

    private void createReceiptPanel() {
        receiptArea = new JTextArea(20, 30);
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        initializeReceipt();

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setPreferredSize(new Dimension(300, 400));
        add(scrollPane, BorderLayout.EAST);
    }

    private void createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBackground(Color.BLACK);

        quantityField = new JTextField(20);
        JButton addButton = new JButton("Add");
        JButton payButton = new JButton("Pay");

        addButton.addActionListener(e -> handleAdd());
        payButton.addActionListener(e -> handlePay());

        controlPanel.add(new JLabel("Antal:") {{ setForeground(Color.WHITE); }});
        controlPanel.add(quantityField);
        controlPanel.add(addButton);
        controlPanel.add(payButton);

        add(controlPanel, BorderLayout.SOUTH);
    }

    private void handleProductClick(Product product) {
        selectedProduct = product; // Spara den valda produkten
        quantityField.setText("1");
    }

    private void handleAdd() {
        try {
            int quantity = Integer.parseInt(quantityField.getText());
            if (quantity <= 0) throw new NumberFormatException();

            if (selectedProduct == null) {
                JOptionPane.showMessageDialog(this, "Välj en produkt först!", "Fel", JOptionPane.ERROR_MESSAGE);
                return;
            }

            OrderItem item = new OrderItem(selectedProduct, quantity);
            currentOrder.add(item);
            updateReceipt();
            quantityField.setText("");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ange ett giltigt antal!", "Fel", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Hanterar betalning och avslutning av ordern
    private void handlePay() {
        if (currentOrder.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No items in the order!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double total = currentOrder.stream().mapToDouble(OrderItem::getTotal).sum();
        double totalVat = currentOrder.stream().mapToDouble(OrderItem::getVatAmount).sum();

        LocalDateTime orderTime = LocalDateTime.now();

        repository.saveOrder(currentOrder, currentReceiptNumber, total, totalVat, orderTime);

        JOptionPane.showMessageDialog(this,
                "TACK FÖR DITT KÖP!\nKvitto #" + currentReceiptNumber,
                "Order Complete",
                JOptionPane.INFORMATION_MESSAGE
        );

        startNewOrder(); // Starta en ny order först NU
    }



    // Skapar ett nytt tomt kvitto och genererar kvittonummer endast vid ny beställning
    private void initializeReceipt() {
        if (currentReceiptNumber == null) { // Om det är första gången, generera kvittonummer
            currentReceiptNumber = generateReceiptNumber();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(currentReceiptNumber).append("\n");
        sb.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("-".repeat(40)).append("\n");
        receiptArea.setText(sb.toString()); // Sätter texten i kvittot
    }


    private void updateReceipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("KVITTO #").append(generateReceiptNumber()).append("\n");
        sb.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("-".repeat(40)).append("\n");

        double total = 0;
        double totalVat = 0;

        for (OrderItem item : currentOrder) {
            sb.append(item.toString()).append("\n");
            total += item.getTotal();
            totalVat += item.getVatAmount();
        }

        sb.append("-".repeat(40)).append("\n");
        sb.append(String.format("Total: %.2f kr\n", total));
        sb.append(String.format("Varav moms: %.2f kr\n", totalVat));

        receiptArea.setText(sb.toString());
    }

    public String generateReceiptNumber() {
        int receiptNumber = 1; // Standardvärde om databasen är tom

        try (Connection conn = se.systementor.DB.DatabaseConnection.getConnection()) {
            // Hämta det senaste kvittonumret
            String query = "SELECT receipt_number FROM receipt_counter ORDER BY id DESC LIMIT 1";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    receiptNumber = rs.getInt("receipt_number") + 1;
                }
            }

            // Uppdatera kvittonumret i databasen
            String updateQuery = "UPDATE receipt_counter SET receipt_number = ? WHERE id = 1";
            try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
                ps.setInt(1, receiptNumber);
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "KVITTO #" + receiptNumber;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CashRegisterGUI gui = new CashRegisterGUI();
            gui.setVisible(true);
        });
    }
    private void startNewOrder() {
        currentReceiptNumber = generateReceiptNumber(); // Generera nytt kvittonummer
        currentOrder.clear(); // Rensa orderlistan
        updateOrderDisplay(); // Uppdatera UI
    }

    private void updateOrderDisplay() {
    }
    private void handleAddProduct(Product product) {
        currentOrder.add(new OrderItem(product, 1)); // Lägg till produkt utan att ändra kvittonummer
        updateOrderDisplay();
    }


}