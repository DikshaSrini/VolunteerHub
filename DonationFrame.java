package miniproject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class DonationFrame extends JFrame {
    private JTextField usernameField;
    private JTextField donationDetailsField;
    private JTextField organizationField;
    private JTextField dateField;
    private JTextField amountField;
    private JTable donationTable;
    private DefaultTableModel tableModel;
    private JTextArea donationHistoryArea;

    public DonationFrame() {
        initializeComponents();
        setupLayout();
    }

    private void initializeComponents() {
        usernameField = new JTextField();
        donationDetailsField = new JTextField();
        organizationField = new JTextField();
        dateField = new JTextField();
        amountField = new JTextField();
        donationHistoryArea = new JTextArea(20, 50); // Set rows and columns for JTextArea
        JScrollPane historyScrollPane = new JScrollPane(donationHistoryArea);

        JButton donateButton = new JButton("Donate");
        donateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                promptForDonation();
            }
        });

        JButton viewHistoryButton = new JButton("View Donation History");
        viewHistoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewDonationHistory();
            }
        });

        tableModel = new DefaultTableModel();
        donationTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(donationTable);

        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10)); // Improved spacing
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Added border for better spacing
        inputPanel.add(new JLabel("Username:"));
        inputPanel.add(usernameField);
        inputPanel.add(new JLabel("What do you wish to Donate?:"));
        inputPanel.add(donationDetailsField);
        inputPanel.add(new JLabel("Organization:"));
        inputPanel.add(organizationField);
        inputPanel.add(new JLabel("Date (YYYY-MM-DD):")); // Added note about date format
        inputPanel.add(dateField);
        inputPanel.add(new JLabel()); // Placeholder for the prompt label
        inputPanel.add(new JLabel()); // Empty label for spacing
        inputPanel.add(new JLabel()); // Empty label for spacing
        inputPanel.add(donateButton);

        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.add(new JLabel("Donation History"), BorderLayout.NORTH);
        historyPanel.add(tableScrollPane, BorderLayout.CENTER);
        historyPanel.add(historyScrollPane, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(new JLabel()); // Empty label for spacing
        buttonPanel.add(viewHistoryButton);

        add(inputPanel, BorderLayout.NORTH);
        add(historyPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Purple theme
        Color purpleColor = new Color(128, 0, 128);
        inputPanel.setBackground(purpleColor);
        historyPanel.setBackground(purpleColor);
        buttonPanel.setBackground(purpleColor);

        // Set foreground (text) color to purple
        Color purpleTextColor = new Color(255, 0, 255); // Purple text color
        usernameField.setForeground(purpleTextColor);
        donationDetailsField.setForeground(purpleTextColor);
        organizationField.setForeground(purpleTextColor);
        dateField.setForeground(purpleTextColor);
        amountField.setForeground(purpleTextColor);
        donationHistoryArea.setForeground(purpleTextColor);

        // Set background color explicitly for better visibility
        usernameField.setBackground(new Color(169, 169, 169)); // Dark Gray
        donationDetailsField.setBackground(new Color(169, 169, 169));
        organizationField.setBackground(new Color(169, 169, 169));
        dateField.setBackground(new Color(169, 169, 169));
        amountField.setBackground(new Color(169, 169, 169));
        donationHistoryArea.setBackground(new Color(255, 218, 185)); // Peach

        // Set button colors
        donateButton.setBackground(new Color(255, 182, 193)); // Light Pink
        viewHistoryButton.setBackground(new Color(255, 182, 193));

        // Set table colors
        donationTable.setBackground(new Color(255, 218, 185)); // Peach
        donationTable.setForeground(Color.BLACK); // Set text color to black
    }

    private void setupLayout() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Donation Frame");
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void promptForDonation() {
        String donationPrompt = "Do you want to additionally donate money? (Yes/No)";
        String userResponse = JOptionPane.showInputDialog(this, donationPrompt);

        if (userResponse != null) {
            userResponse = userResponse.trim().toLowerCase(); // Convert to lowercase for case-insensitivity

            if (userResponse.equals("yes")) {
                // Prompt for donation amount
                String donationAmount = JOptionPane.showInputDialog(this, "Enter donation amount:");
                if (donationAmount != null && !donationAmount.isEmpty()) {
                    amountField.setText(donationAmount);
                } else {
                    // If the amount is empty, consider it as zero
                    amountField.setText("0");
                }
            } else if (userResponse.equals("no")) {
                // If the response is "no", set the amount to zero
                amountField.setText("0");
            } else {
                // Handle invalid input
                JOptionPane.showMessageDialog(this, "Invalid response. Please enter 'Yes' or 'No'.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            donate(); // Proceed with donation after handling the user response
        }
    }

    private void donate() {
        try {
            Connection connection = DatabaseManager.getConnection();
            String query = "INSERT INTO donations (donor_username, donation_details, organization, donation_date, amount) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, usernameField.getText());
                statement.setString(2, donationDetailsField.getText());
                statement.setString(3, organizationField.getText());

                // Validate date format
                if (isValidDateFormat(dateField.getText())) {
                    statement.setString(4, dateField.getText());
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validate amount
                try {
                    double donationAmount = Double.parseDouble(amountField.getText());
                    statement.setDouble(5, donationAmount);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid amount. Please enter a valid numeric amount.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                statement.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Donation successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

            // Refresh donation history after donation
            viewDonationHistory();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error during donation: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewDonationHistory() {
        try {
            Connection connection = DatabaseManager.getConnection();
            String query = "SELECT * FROM donations WHERE donor_username=?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, usernameField.getText());
                ResultSet resultSet = statement.executeQuery();

                tableModel.setRowCount(0);
                donationHistoryArea.setText(""); // Clear existing text

                while (resultSet.next()) {
                    int donationID = resultSet.getInt("donation_id");
                    String donationDetails = resultSet.getString("donation_details");
                    String organization = resultSet.getString("organization");
                    String donationDate = resultSet.getString("donation_date");
                    double amount = resultSet.getDouble("amount");

                    // Display donation history in JTextArea
                    donationHistoryArea.append("Donation ID: " + donationID + "\n");
                    donationHistoryArea.append("Donation Details: " + donationDetails + "\n");
                    donationHistoryArea.append("Organization: " + organization + "\n");
                    donationHistoryArea.append("Donation Date: " + donationDate + "\n");
                    donationHistoryArea.append("Amount: " + amount + "\n");
                    donationHistoryArea.append("----------------------------\n");

                    // Do not display amount on the main screen table
                    tableModel.addRow(new Object[]{donationID, donationDetails, organization, donationDate});
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while retrieving donation history: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unexpected error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isValidDateFormat(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            sdf.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DonationFrame());
    }
}
