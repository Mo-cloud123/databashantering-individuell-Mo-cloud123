package se.systementor;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CashRegisterGUI gui = new CashRegisterGUI();
            gui.setVisible(true); // Visar fönstret
        });
    }
    }