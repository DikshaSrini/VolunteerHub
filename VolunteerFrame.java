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

public class VolunteerFrame extends JFrame {
    private JTextField usernameField;
    private JButton viewEventsButton;
    private JTextField registerEventField;
    private JButton registerButton;
    private JTable eventsTable;
    private DefaultTableModel tableModel;

    public VolunteerFrame() {
        initializeComponents();
        setupLayout();
    }

    private void initializeComponents() {
        JPanel userPanel = new JPanel(new FlowLayout());
        usernameField = new JTextField(15);
        viewEventsButton = new JButton("View Available Events");
        viewEventsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewAvailableEvents();
            }
        });
        userPanel.add(new JLabel("Username:"));
        userPanel.add(usernameField);
        userPanel.add(viewEventsButton);

        JPanel registerPanel = new JPanel(new FlowLayout());
        registerEventField = new JTextField(5);
        registerButton = new JButton("Register for Event");
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                registerForEvent();
            }
        });
        registerPanel.add(new JLabel("Event ID:"));
        registerPanel.add(registerEventField);
        registerPanel.add(registerButton);

        tableModel = new DefaultTableModel();
        eventsTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(eventsTable);

        setLayout(new BorderLayout());
        add(userPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(registerPanel, BorderLayout.SOUTH);

        // Apply theme
        Color purpleColor = new Color(128, 0, 128);
        userPanel.setBackground(purpleColor);
        registerPanel.setBackground(purpleColor);

        Color purpleTextColor = new Color(255, 0, 255);
        usernameField.setForeground(purpleTextColor);
        viewEventsButton.setForeground(purpleTextColor);
        registerEventField.setForeground(purpleTextColor);
        registerButton.setForeground(purpleTextColor);

        usernameField.setBackground(new Color(169, 169, 169));
        viewEventsButton.setBackground(new Color(255, 182, 193));
        registerEventField.setBackground(new Color(169, 169, 169));
        registerButton.setBackground(new Color(255, 182, 193));

        eventsTable.setBackground(new Color(255, 218, 185));
        eventsTable.setForeground(Color.BLACK);
    }

    private void setupLayout() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Volunteer Frame");
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void viewAvailableEvents() {
        try {
            Connection connection = DatabaseManager.getConnection();
            String query = "SELECT * FROM events";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                ResultSet resultSet = statement.executeQuery();

                // Set column names
                tableModel.setColumnIdentifiers(new Object[]{"Event ID", "Event Name", "Event Date"});

                // Clear existing rows
                tableModel.setRowCount(0);

                // Add rows from the result set
                while (resultSet.next()) {
                    int eventID = resultSet.getInt("event_id");
                    String eventName = resultSet.getString("event_name");
                    String eventDate = resultSet.getString("event_date");

                    tableModel.addRow(new Object[]{eventID, eventName, eventDate});
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while retrieving available events: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerForEvent() {
        try {
            Connection connection = DatabaseManager.getConnection();

            // Check if the event ID is provided
            if (registerEventField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter the Event ID to register for an event.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Parse the event ID
            int eventID;
            try {
                eventID = Integer.parseInt(registerEventField.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid Event ID. Please enter a valid numeric ID.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if the event ID exists
            String checkQuery = "SELECT * FROM events WHERE event_id=?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                checkStatement.setInt(1, eventID);
                ResultSet resultSet = checkStatement.executeQuery();

                if (!resultSet.next()) {
                    JOptionPane.showMessageDialog(this, "Event with ID " + eventID + " does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Perform the registration (this is a basic example, and you might want to improve this)
            String registerQuery = "INSERT INTO registrations (username, event_id) VALUES (?, ?)";
            try (PreparedStatement registerStatement = connection.prepareStatement(registerQuery)) {
                registerStatement.setString(1, usernameField.getText());
                registerStatement.setInt(2, eventID);
                registerStatement.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error during registration: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VolunteerFrame());
    }
}
