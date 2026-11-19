import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    public Main() {

        setTitle("BeReal Database System");
        setSize(500, 400);
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
        JButton advancedQueriesButton = new JButton("Advanced Queries & Admin");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5, 1, 10, 10));

        buttonPanel.add(usersButton);
        buttonPanel.add(postsButton);
        buttonPanel.add(friendshipButton);
        buttonPanel.add(interactionsButton);
        buttonPanel.add(advancedQueriesButton);

        JPanel centerPanel = new JPanel();
        centerPanel.setBorder(
            BorderFactory.createEmptyBorder(40, 80, 40, 80)
        );
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(buttonPanel, BorderLayout.CENTER);

        add(titleLabel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        // Open Posts GUI
        postsButton.addActionListener(e -> {
            PostGUI postGUI = new PostGUI();
            postGUI.setVisible(true);
        });

        // Temporary button until User Management GUI is completed
        usersButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                this,
                "User Management GUI will be added later."
            );
        });

        // Temporary button until Friendship GUI is completed
        friendshipButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                this,
                "Friendship GUI will be added later."
            );
        });

        // Temporary button until Interactions GUI is completed
        interactionsButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                this,
                "Interactions GUI will be added later."
            );
        });

        // Open Advanced Queries GUI
        advancedQueriesButton.addActionListener(e -> {
            AdvancedQueriesGUI advancedGUI = new AdvancedQueriesGUI();
            advancedGUI.setVisible(true);
        });
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            Main menu = new Main();
            menu.setVisible(true);
        });
    }
}