import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/** Friend requests for the currently signed-in account. Database IDs stay internal. */
public class FriendshipManagement extends JFrame {
    private final int currentUserId;
    private final JTextField receiverUsernameField = new JTextField(18);
    private final JLabel signedInLabel = new JLabel("Loading account...");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"From", "Requested on"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable pendingTable = new JTable(tableModel);

    // Maps displayed rows to internal friendship IDs; IDs are never entered by users.
    private final java.util.List<Integer> pendingIds = new java.util.ArrayList<>();

    public FriendshipManagement(int userId) {
        super("BeReal - Friends");
        if (userId <= 0) throw new IllegalArgumentException("Sign in first.");
        this.currentUserId = userId;
        BeRealTheme.install();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(750, 540);
        setMinimumSize(new Dimension(610, 440));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(14, 18));
        root.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        setContentPane(root);

        JPanel top = new JPanel(new BorderLayout(0, 8));
        JLabel title = new JLabel("BeReal | Friends");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        top.add(title, BorderLayout.NORTH);
        top.add(signedInLabel, BorderLayout.SOUTH);
        root.add(top, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(10, 14));
        JPanel send = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        send.setBorder(BorderFactory.createTitledBorder("Send friend request"));
        send.add(new JLabel("Friend's username:"));
        send.add(receiverUsernameField);
        JButton sendButton = new JButton("Send Request");
        sendButton.addActionListener(e -> sendRequest());
        receiverUsernameField.addActionListener(e -> sendRequest());
        send.add(sendButton);
        content.add(send, BorderLayout.NORTH);

        JPanel pending = new JPanel(new BorderLayout(8, 10));
        pending.setBorder(BorderFactory.createTitledBorder("Your incoming friend requests"));
        pendingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pendingTable.setFillsViewportHeight(true);
        pending.add(new JScrollPane(pendingTable), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        JButton acceptButton = new JButton("Accept Selected Request");
        refreshButton.addActionListener(e -> loadPendingRequests());
        acceptButton.addActionListener(e -> acceptSelectedRequest());
        actions.add(refreshButton);
        actions.add(acceptButton);
        pending.add(actions, BorderLayout.SOUTH);
        content.add(pending, BorderLayout.CENTER);
        root.add(content, BorderLayout.CENTER);
        BeRealTheme.apply(root);
        loadUsername();
        loadPendingRequests();
    }

    /** Prevent opening an unauthenticated standalone friend-management screen. */
    public FriendshipManagement() {
        this(-1);
    }

    private void loadUsername() {
        String sql = "SELECT Username FROM `USER` WHERE UserID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                signedInLabel.setText(rs.next() ? "Signed in as @" + rs.getString("Username")
                        : "Account not found");
            }
        } catch (SQLException ex) { showDatabaseError(ex); }
    }

    private int findUserId(Connection conn, String username) throws SQLException {
        String sql = "SELECT UserID FROM `USER` WHERE Username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) { return rs.next() ? rs.getInt(1) : -1; }
        }
    }

    private void sendRequest() {
        String username = receiverUsernameField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter the friend's username.");
            return;
        }
        String checkSql = "SELECT COUNT(*) FROM FRIENDSHIP " +
                "WHERE (Requester_ID = ? AND Receiver_ID = ?) " +
                "OR (Requester_ID = ? AND Receiver_ID = ?)";
        String insertSql = "INSERT INTO FRIENDSHIP " +
                "(Date_Requested, Requester_ID, Receiver_ID, Status) " +
                "VALUES (CURRENT_DATE, ?, ?, 'Pending')";
        try (Connection conn = DBConnection.getConnection()) {
            int receiverId = findUserId(conn, username);
            if (receiverId == -1) {
                JOptionPane.showMessageDialog(this, "No account found with username @" + username + ".");
                return;
            }
            if (receiverId == currentUserId) {
                JOptionPane.showMessageDialog(this, "You cannot send a request to yourself.");
                return;
            }
            try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                check.setInt(1, currentUserId);
                check.setInt(2, receiverId);
                check.setInt(3, receiverId);
                check.setInt(4, currentUserId);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this,
                                "A friend request or friendship already exists between you and @" + username + ".");
                        return;
                    }
                }
            }
            try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                insert.setInt(1, currentUserId);
                insert.setInt(2, receiverId);
                insert.executeUpdate();
            }
            receiverUsernameField.setText("");
            JOptionPane.showMessageDialog(this, "Friend request sent to @" + username + ".");
        } catch (SQLException ex) { showDatabaseError(ex); }
    }

    private void acceptSelectedRequest() {
        int viewRow = pendingTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a request from the table first.");
            return;
        }
        int modelRow = pendingTable.convertRowIndexToModel(viewRow);
        int friendshipId = pendingIds.get(modelRow);
        String sender = String.valueOf(tableModel.getValueAt(modelRow, 0));
        String sql = "UPDATE FRIENDSHIP SET Status = 'Accepted', ApprovedDate = CURRENT_DATE " +
                "WHERE Friendship_id = ? AND Receiver_ID = ? AND Status = 'Pending'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, friendshipId);
            stmt.setInt(2, currentUserId);
            int updated = stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, updated == 1
                    ? "You and " + sender + " are now friends."
                    : "That request is no longer pending.");
            loadPendingRequests();
        } catch (SQLException ex) { showDatabaseError(ex); }
    }

    private void loadPendingRequests() {
        String sql = "SELECT F.Friendship_id, U.Username, F.Date_Requested " +
                "FROM FRIENDSHIP F JOIN `USER` U ON F.Requester_ID = U.UserID " +
                "WHERE F.Status = 'Pending' AND F.Receiver_ID = ? " +
                "ORDER BY F.Date_Requested DESC, F.Friendship_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                tableModel.setRowCount(0);
                pendingIds.clear();
                while (rs.next()) {
                    pendingIds.add(rs.getInt("Friendship_id"));
                    tableModel.addRow(new Object[]{
                            "@" + rs.getString("Username"), rs.getDate("Date_Requested")});
                }
            }
        } catch (SQLException ex) { showDatabaseError(ex); }
    }

    private void showDatabaseError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
