import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class PostGUI extends JFrame {

    public PostGUI() {
        setTitle("BeReal Posts");
        setSize(550, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

    private void viewPosts() {
        try {
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM POST");

            String posts = "";

            while (rs.next()) {
                posts += "Post ID: " + rs.getInt("PostID") + "\n";
                posts += "Front Camera: " + rs.getString("FrontCam") + "\n";
                posts += "Back Camera: " + rs.getString("BackCam") + "\n";
                posts += "Caption: " + rs.getString("Caption") + "\n";
                posts += "Retake Count: " + rs.getInt("RetakeCount") + "\n";
                posts += "Hours Late: " + rs.getDouble("HoursLate") + "\n";
                posts += "Posted At: " + rs.getString("PostedAt") + "\n";
                posts += "User ID: " + rs.getInt("UserID") + "\n";
                posts += "Moment ID: " + rs.getInt("MomentID") + "\n";
                posts += "-------------------------\n";
            }

            JTextArea textArea = new JTextArea(posts);
            textArea.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(420, 260));

            JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Posts",
                JOptionPane.INFORMATION_MESSAGE
            );

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error: " + e.getMessage()
            );
        }
    }

    private void createPost() {

        JTextField frontCamField = new JTextField();
        JTextField backCamField = new JTextField();
        JTextField captionField = new JTextField();
        JTextField retakeCountField = new JTextField();
        JTextField hoursLateField = new JTextField();
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
            this,
            fields,
            "Create Post",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {

            String sql = "INSERT INTO POST " +
                         "(FrontCam, BackCam, Caption, RetakeCount, HoursLate, PostedAt, UserID, MomentID) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try {
                Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);

                pstmt.setString(1, frontCamField.getText());
                pstmt.setString(2, backCamField.getText());
                pstmt.setString(3, captionField.getText());
                pstmt.setInt(4, Integer.parseInt(retakeCountField.getText()));
                pstmt.setDouble(5, Double.parseDouble(hoursLateField.getText()));
                pstmt.setString(6, postedAtField.getText());
                pstmt.setInt(7, Integer.parseInt(userIDField.getText()));
                pstmt.setInt(8, Integer.parseInt(momentIDField.getText()));

                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(
                    this,
                    "Post created successfully!"
                );

                pstmt.close();
                conn.close();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error: " + e.getMessage()
                );
            }
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
            this,
            fields,
            "Update Post",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {

            String sql = "UPDATE POST SET Caption = ? WHERE PostID = ?";

            try {
                Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);

                pstmt.setString(1, captionField.getText());
                pstmt.setInt(2, Integer.parseInt(postIDField.getText()));

                int rowsUpdated = pstmt.executeUpdate();

                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Post updated successfully!"
                    );
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "Post not found."
                    );
                }

                pstmt.close();
                conn.close();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error: " + e.getMessage()
                );
            }
        }
    }

    private void deletePost() {

        String postID = JOptionPane.showInputDialog(
            this,
            "Enter Post ID to delete:"
        );

        if (postID == null || postID.trim().isEmpty()) {
            return;
        }

        String sql = "DELETE FROM POST WHERE PostID = ?";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, Integer.parseInt(postID));

            int rowsDeleted = pstmt.executeUpdate();

            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "Post deleted successfully!"
                );
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Post not found."
                );
            }

            pstmt.close();
            conn.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error: " + e.getMessage()
            );
        }
    }

    public static void main(String[] args) {
        PostGUI frame = new PostGUI();
        frame.setVisible(true);
    }
}