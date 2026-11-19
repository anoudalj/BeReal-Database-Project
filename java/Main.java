import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    public Main() {

        setTitle("BeReal Database System");
        setSize(500, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel(
            "BeReal Database Management",
            SwingConstants.CENTER
        );
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));

        JButton usersButton = new JButton("User Management");
        JButton postsButton = new JButton("Posts & Moments");
        JButton friendshipButton = new JButton("Friendships");
        JButton interactionsButton = new JButton("Interactions");
        JButton queriesButton = new JButton("Advanced Queries & Admin");

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        buttonPanel.add(usersButton);
        buttonPanel.add(postsButton);
        buttonPanel.add(friendshipButton);
        buttonPanel.add(interactionsButton);
        buttonPanel.add(queriesButton);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 80, 30, 80));
        centerPanel.add(buttonPanel, BorderLayout.CENTER);

        add(titleLabel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        usersButton.addActionListener(e ->
            new UserManagementGUI().setVisible(true));

        postsButton.addActionListener(e ->
            new PostGUI().setVisible(true));

        friendshipButton.addActionListener(e ->
            new FriendshipManagement().setVisible(true));

        interactionsButton.addActionListener(e -> openInteractions());

        queriesButton.addActionListener(e ->
            new AdvancedQueriesGUI().setVisible(true));
    }

    // InteractionsGUI needs to know which user is acting, so ask for a User ID first
    private void openInteractions() {
        String input = JOptionPane.showInputDialog(this, "Enter your User ID:");

        if (input == null) {
            return; // cancelled
        }

        try {
            int userID = Integer.parseInt(input.trim());
            if (userID <= 0) {
                throw new NumberFormatException();
            }
            new InteractionsGUI(userID).setVisible(true);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid positive User ID.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
