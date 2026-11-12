import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FriendshipManagement extends JFrame {
    private final JTextField requesterField = new JTextField(10);
    private final JTextField receiverField = new JTextField(10);
    private final JTextField friendshipField = new JTextField(10);
    private final JTextField acceptingReceiverField = new JTextField(10);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Friendship ID", "Requester", "Receiver ID", "Date Requested"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    public FriendshipManagement() {
        super("BeReal - Friendship Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 550);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setContentPane(root);

        JLabel title = new JLabel("BeReal | Friendship Management");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        root.add(title, BorderLayout.NORTH);

        JPanel forms = new JPanel(new GridLayout(2, 1, 10, 10));
        JPanel sendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        sendPanel.setBorder(BorderFactory.createTitledBorder("Send Friend Request"));
        sendPanel.add(new JLabel("Requester ID:"));
        sendPanel.add(requesterField);
        sendPanel.add(new JLabel("Receiver ID:"));
        sendPanel.add(receiverField);
        JButton sendButton = new JButton("Send Request");
        sendButton.addActionListener(e -> sendRequest());
        sendPanel.add(sendButton);
        forms.add(sendPanel);

        JPanel acceptPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        acceptPanel.setBorder(BorderFactory.createTitledBorder("Accept Friend Request"));
        acceptPanel.add(new JLabel("Friendship ID:"));
        acceptPanel.add(friendshipField);
        acceptPanel.add(new JLabel("Receiver ID:"));
        acceptPanel.add(acceptingReceiverField);
        JButton acceptButton = new JButton("Accept Request");
        acceptButton.addActionListener(e -> acceptRequest());
        acceptPanel.add(acceptButton);
        forms.add(acceptPanel);
        root.add(forms, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        JPanel heading = new JPanel(new FlowLayout(FlowLayout.LEFT));
        heading.add(new JLabel("Pending Friendship Requests (Admin View)"));
        JButton refreshButton = new JButton("Refresh Pending Requests");
        refreshButton.addActionListener(e -> loadPendingRequests());
        heading.add(refreshButton);
        bottom.add(heading, BorderLayout.NORTH);
        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        bottom.add(new JScrollPane(table), BorderLayout.CENTER);
        bottom.setPreferredSize(new Dimension(750, 210));
        root.add(bottom, BorderLayout.SOUTH);
    }

    private int parseId(JTextField field, String label) {
        try {
            int id = Integer.parseInt(field.getText().trim());
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a positive integer.");
        }
    }

    private void sendRequest() {
        try {
            int requesterId = parseId(requesterField, "Requester ID");
            int receiverId = parseId(receiverField, "Receiver ID");
            if (requesterId == receiverId) {
                throw new IllegalArgumentException("You cannot send a request to yourself.");
            }

            String checkSql = "SELECT COUNT(*) FROM FRIENDSHIP " +
                    "WHERE (Requester_ID = ? AND Receiver_ID = ?) " +
                    "OR (Requester_ID = ? AND Receiver_ID = ?)";
            String insertSql = "INSERT INTO FRIENDSHIP " +
                    "(Date_Requested, Requester_ID, Receiver_ID, Status) " +
                    "VALUES (CURRENT_DATE, ?, ?, 'Pending')";

            try (Connection conn = DBConnection.getConnection()) {
                try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                    check.setInt(1, requesterId);
                    check.setInt(2, receiverId);
                    check.setInt(3, receiverId);
                    check.setInt(4, requesterId);
                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            JOptionPane.showMessageDialog(this,
                                    "A friendship request or friendship already exists between these users.");
                            return;
                        }
                    }
                }
                try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                    insert.setInt(1, requesterId);
                    insert.setInt(2, receiverId);
                    insert.executeUpdate();
                }
            }
            JOptionPane.showMessageDialog(this, "Friend request sent successfully!");
            loadPendingRequests();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void acceptRequest() {
        try {
            int friendshipId = parseId(friendshipField, "Friendship ID");
            int receiverId = parseId(acceptingReceiverField, "Receiver ID");
            String sql = "UPDATE FRIENDSHIP SET Status = 'Accepted', " +
                    "ApprovedDate = CURRENT_DATE " +
                    "WHERE Friendship_id = ? AND Receiver_ID = ? AND Status = 'Pending'";
            int updated;
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, friendshipId);
                stmt.setInt(2, receiverId);
                updated = stmt.executeUpdate();
            }
            if (updated == 1) {
                JOptionPane.showMessageDialog(this, "Friend request accepted!");
                loadPendingRequests();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No matching pending request found for that friendship and receiver ID.");
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void loadPendingRequests() {
        String sql = "SELECT F.Friendship_id, U.Username AS Requester, " +
                "F.Receiver_ID, F.Date_Requested " +
                "FROM FRIENDSHIP F JOIN `USER` U ON F.Requester_ID = U.UserID " +
                "WHERE F.Status = 'Pending' ORDER BY F.Date_Requested DESC, F.Friendship_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("Friendship_id"), rs.getString("Requester"),
                        rs.getInt("Receiver_ID"), rs.getDate("Date_Requested")
                });
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void showDatabaseError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FriendshipManagement().setVisible(true));
    }
}
