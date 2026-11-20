import java.io.Console;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Restricted local administrative bootstrap tool. NEVER expose this as an app GUI button.
 * First create a regular account through the app; this tool promotes it to Admin.
 * Access requires ADMIN_SETUP_KEY configured outside source control.
 */
public final class AdminSetup {
    private AdminSetup() { }

    public static void main(String[] args) {
        Console console = System.console();
        if (console == null) {
            System.err.println("Run this utility in a terminal (not inside the Swing app or an IDE console).");
            return;
        }
        String secret = System.getenv("ADMIN_SETUP_KEY");
        if (secret == null || secret.length() < 6) {
            System.err.println("ADMIN_SETUP_KEY must be set to a secret of at least 6 characters.");
            return;
        }
        char[] supplied = console.readPassword("Admin setup key: ");
        if (supplied == null) return;
        byte[] expected = secret.getBytes(StandardCharsets.UTF_8);
        byte[] entered = new String(supplied).getBytes(StandardCharsets.UTF_8);
        Arrays.fill(supplied, '\0');
        boolean authorized = MessageDigest.isEqual(expected, entered);
        Arrays.fill(entered, (byte) 0);
        Arrays.fill(expected, (byte) 0);
        if (!authorized) {
            System.err.println("Access denied.");
            return;
        }

        String username = console.readLine("Existing account username (not email): ");
        if (username == null || username.trim().isEmpty()) {
            System.err.println("A username is required.");
            return;
        }
        username = username.trim();
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId;
                String role;
                try (PreparedStatement find = conn.prepareStatement(
                        "SELECT UserID, UserType FROM `USER` WHERE Username = ? FOR UPDATE")) {
                    find.setString(1, username);
                    try (ResultSet rs = find.executeQuery()) {
                        if (!rs.next()) {
                            System.err.println("No account found with that username. Create it through the app first.");
                            conn.rollback();
                            return;
                        }
                        userId = rs.getInt("UserID");
                        role = rs.getString("UserType");
                    }
                }
                if ("Admin".equalsIgnoreCase(role)) {
                    System.out.println("This account is already an admin.");
                    conn.rollback();
                    return;
                }
                if (!"Regular".equalsIgnoreCase(role) && !"User".equalsIgnoreCase(role)) {
                    System.err.println("Unexpected UserType: " + role + ". No changes made.");
                    conn.rollback();
                    return;
                }
                String confirmation = console.readLine(
                        "Promote " + username + " (UserID " + userId + ") to Admin? Type PROMOTE to confirm: ");
                if (!"PROMOTE".equals(confirmation)) {
                    System.out.println("Cancelled.");
                    conn.rollback();
                    return;
                }
                try (PreparedStatement change = conn.prepareStatement(
                        "UPDATE `USER` SET UserType = 'Admin' WHERE UserID = ? AND UserType = ?")) {
                    change.setInt(1, userId);
                    change.setString(2, role);
                    if (change.executeUpdate() != 1) {
                        throw new SQLException("Expected exactly one account to be updated.");
                    }
                }
                conn.commit();
                System.out.println("Admin account configured. Log in through Main using the existing password and Admin role.");
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            System.err.println("Database operation failed: " + ex.getMessage());
        }
    }
}
