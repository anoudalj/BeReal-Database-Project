import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;

public class PostGUI extends JFrame {

    public PostGUI() {
        setTitle("BeReal Posts");
        setSize(550, 400);
        // DISPOSE (not EXIT) so closing this window does not close the whole app
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel("Post Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));

        JButton viewButton = new JButton("View Posts");
        JButton addButton = new JButton("Create Post");
        JButton updateButton = new JButton("Update Post");
        JButton deleteButton = new JButton("Delete Post");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(viewButton);
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        add(titleLabel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);

        viewButton.addActionListener(e -> viewPosts());
        addButton.addActionListener(e -> createPost());
        updateButton.addActionListener(e -> updatePost());
        deleteButton.addActionListener(e -> deletePost());
    }

    private void showError(Exception e) {
        String msg;
        if (e instanceof NumberFormatException) {
            msg = "Please enter valid numbers for the numeric fields.";
        } else if (e instanceof SQLIntegrityConstraintViolationException) {
            msg = "The User ID or Moment ID does not exist, " +
                  "or this post is still referenced by other records.";
        } else {
            msg = e.getMessage();
        }
        JOptionPane.showMessageDialog(this, "Error: " + msg);
    }

    private void viewPosts() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM POST")) {

            StringBuilder posts = new StringBuilder();

            while (rs.next()) {
                posts.append("Post ID: ").append(rs.getInt("PostID")).append("\n");
                posts.append("Front Camera: ").append(rs.getString("FrontCam")).append("\n");
                posts.append("Back Camera: ").append(rs.getString("BackCam")).append("\n");
                posts.append("Caption: ").append(rs.getString("Caption")).append("\n");
                posts.append("Retake Count: ").append(rs.getInt("RetakeCount")).append("\n");
                posts.append("Hours Late: ").append(rs.getDouble("HoursLate")).append("\n");
                posts.append("Posted At: ").append(rs.getString("PostedAt")).append("\n");
                posts.append("User ID: ").append(rs.getInt("UserID")).append("\n");
                posts.append("Moment ID: ").append(rs.getInt("MomentID")).append("\n");
                posts.append("-------------------------\n");
            }

            if (posts.length() == 0) {
                posts.append("No posts found.");
            }

            JTextArea textArea = new JTextArea(posts.toString());
            textArea.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(420, 260));

            JOptionPane.showMessageDialog(
                this, scrollPane, "Posts", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            showError(e);
        }
    }

    private void createPost() {

        JTextField frontCamField = new JTextField();
        JTextField backCamField = new JTextField();
        JTextField captionField = new JTextField();
        JTextField retakeCountField = new JTextField("0");
        JTextField hoursLateField = new JTextField("0");
        JTextField postedAtField = new JTextField();
        JTextField userIDField = new JTextField();
        JTextField momentIDField = new JTextField();

        Object[] fields = {
            "Front Camera:", frontCamField,
            "Back Camera:", backCamField,
            "Caption:", captionField,
            "Retake Count:", retakeCountField,
            "Hours Late:", hoursLateField,
            "Posted At (YYYY-MM-DD HH:MM:SS):", postedAtField,
            "User ID:", userIDField,
            "Moment ID:", momentIDField
        };

        int option = JOptionPane.showConfirmDialog(
            this, fields, "Create Post", JOptionPane.OK_CANCEL_OPTION);

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        if (frontCamField.getText().trim().isEmpty()
                || backCamField.getText().trim().isEmpty()
                || postedAtField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Front camera, back camera and posted-at time are required.");
            return;
        }

        String sql = "INSERT INTO POST " +
                     "(FrontCam, BackCam, Caption, RetakeCount, HoursLate, PostedAt, UserID, MomentID) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, frontCamField.getText().trim());
            pstmt.setString(2, backCamField.getText().trim());
            pstmt.setString(3, captionField.getText());
            pstmt.setInt(4, Integer.parseInt(retakeCountField.getText().trim()));
            pstmt.setDouble(5, Double.parseDouble(hoursLateField.getText().trim()));
            pstmt.setString(6, postedAtField.getText().trim());
            pstmt.setInt(7, Integer.parseInt(userIDField.getText().trim()));
            pstmt.setInt(8, Integer.parseInt(momentIDField.getText().trim()));

            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Post created successfully!");

        } catch (Exception e) {
            showError(e);
        }
    }

    private void updatePost() {

        JTextField postIDField = new JTextField();
        JTextField captionField = new JTextField();

        Object[] fields = {
            "Post ID:", postIDField,
            "New Caption:", captionField
        };

        int option = JOptionPane.showConfirmDialog(
            this, fields, "Update Post", JOptionPane.OK_CANCEL_OPTION);

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String sql = "UPDATE POST SET Caption = ? WHERE PostID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, captionField.getText());
            pstmt.setInt(2, Integer.parseInt(postIDField.getText().trim()));

            int rowsUpdated = pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this,
                rowsUpdated > 0 ? "Post updated successfully!" : "Post not found.");

        } catch (Exception e) {
            showError(e);
        }
    }

    private void deletePost() {

        String postID = JOptionPane.showInputDialog(this, "Enter Post ID to delete:");

        if (postID == null || postID.trim().isEmpty()) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Delete post " + postID.trim() + "? Its comments, reactions and tags will be deleted too.",
            "Confirm delete",
            JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM POST WHERE PostID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(postID.trim()));

            int rowsDeleted = pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this,
                rowsDeleted > 0 ? "Post deleted successfully!" : "Post not found.");

        } catch (Exception e) {
            showError(e);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PostGUI().setVisible(true));
    }
}
