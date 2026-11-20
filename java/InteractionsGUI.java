import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.io.File;

public class InteractionsGUI extends JFrame {

    private final int currentUserID;

    private JComboBox<PostItem> postBox;
    private JLabel photoLabel;
    private JLabel captionLabel;

    private JTextArea commentInput;
    private JTextField commentNumberInput;
    private JComboBox<RealMojiOption> realMojiBox;
    private JTextField taggedUserInput;

    private JTable commentsTable;
    private JTable reactionsTable;
    private JTable tagsTable;

    private final Color BG = Color.BLACK;
    private final Color CARD = BeRealTheme.SURFACE;

    public InteractionsGUI(int userID) {
        BeRealTheme.install();
        this.currentUserID = userID;

        setTitle("BeReal - Interactions");
        setSize(800, 750);
        setMinimumSize(new Dimension(650, 600));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        buildGUI();
        loadRealMojis();
        loadPosts();
    }

    public InteractionsGUI(int userID, int postID) {
        this(userID);
        for (int i=0; i<postBox.getItemCount(); i++) {
            if (postBox.getItemAt(i).id == postID) { postBox.setSelectedIndex(i); break; }
        }
    }

    private void buildGUI() {

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(BG);
        main.setBorder(
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        );

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);

        JLabel logo = new JLabel("BeReal.");
        logo.setFont(new Font("Arial", Font.BOLD, 30));
        logo.setForeground(Color.WHITE);

        // Optional image logo
        File logoFile = new File("images/bereal-logo.png");
        if (logoFile.exists()) {
            ImageIcon original = new ImageIcon(logoFile.getPath());
            Image scaled = original.getImage()
                    .getScaledInstance(110, 40, Image.SCALE_SMOOTH);
            logo.setText("");
            logo.setIcon(new ImageIcon(scaled));
        }

        JLabel userLabel = new JLabel("User ID: " + currentUserID);
        userLabel.setForeground(Color.LIGHT_GRAY);

        header.add(logo, BorderLayout.WEST);
        header.add(userLabel, BorderLayout.EAST);

        main.add(header, BorderLayout.NORTH);

        // Main content
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(BG);

        JPanel postPanel = createPostPanel();
        content.add(postPanel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();

tabs.setBackground(BeRealTheme.SURFACE);
tabs.setForeground(Color.WHITE);
tabs.setFont(new Font("Arial", Font.BOLD, 14));

tabs.addTab("Comments", createCommentsPanel());
tabs.addTab("Reactions", createReactionsPanel());
tabs.addTab("Tags", createTagsPanel());

for (int i = 0; i < tabs.getTabCount(); i++) {
    tabs.setBackgroundAt(i, BeRealTheme.SURFACE);
    tabs.setForegroundAt(i, Color.WHITE);
}
        content.add(tabs, BorderLayout.CENTER);

        main.add(content, BorderLayout.CENTER);
        BeRealTheme.apply(main);
        setContentPane(main);
    }

    private JPanel createPostPanel() {

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(CARD);
        panel.setBorder(
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );

        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selector.setBackground(CARD);

        JLabel postLabel = whiteLabel("Select Post:");
        postBox = new JComboBox<>();
        postBox.setPreferredSize(new Dimension(250, 28));

        JButton refresh = new JButton("Refresh Posts");
        refresh.addActionListener(e -> loadPosts());

        selector.add(postLabel);
        selector.add(postBox);
        selector.add(refresh);

        panel.add(selector, BorderLayout.NORTH);

        photoLabel = new JLabel("No post selected", SwingConstants.CENTER);
        photoLabel.setForeground(Color.LIGHT_GRAY);
        photoLabel.setPreferredSize(new Dimension(450, 180));

        captionLabel = whiteLabel("Select a post to view its caption.");
        captionLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel photoArea = new JPanel(new BorderLayout(5, 5));
        photoArea.setBackground(CARD);
        photoArea.add(photoLabel, BorderLayout.CENTER);
        photoArea.add(captionLabel, BorderLayout.SOUTH);

        panel.add(photoArea, BorderLayout.CENTER);

        postBox.addActionListener(e -> {
            showSelectedPost();
            refreshAll();
        });

        return panel;
    }

    private JPanel createCommentsPanel() {

        JPanel panel = interactionPanel();

        commentInput = new JTextArea(3, 30);
        commentInput.setLineWrap(true);
        commentInput.setWrapStyleWord(true);

        commentNumberInput = new JTextField(5);

        JPanel form = new JPanel(new BorderLayout(5, 5));
        form.add(new JScrollPane(commentInput), BorderLayout.CENTER);

        JPanel numberPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        numberPanel.add(new JLabel("Select your comment in the table to delete it."));
        form.add(numberPanel, BorderLayout.SOUTH);

        panel.add(form, BorderLayout.NORTH);

        commentsTable = new JTable();
        panel.add(new JScrollPane(commentsTable), BorderLayout.CENTER);

        JPanel buttons = buttonPanel();

        JButton add = new JButton("Add Comment");
        JButton delete = new JButton("Delete Comment");
        JButton view = new JButton("View Comments");

        add.addActionListener(e -> addComment());
        delete.addActionListener(e -> deleteComment());
        view.addActionListener(e -> viewComments());

        buttons.add(add);
        buttons.add(delete);
        buttons.add(view);

        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createReactionsPanel() {

        JPanel panel = interactionPanel();

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("My RealMoji:"));
        realMojiBox = new JComboBox<>();
        realMojiBox.setPreferredSize(new Dimension(200,30));
        form.add(realMojiBox);
        JButton load=BeRealTheme.button("Refresh RealMojis",false);
        load.addActionListener(e->loadRealMojis());form.add(load);

        panel.add(form, BorderLayout.NORTH);

        reactionsTable = new JTable();
        panel.add(new JScrollPane(reactionsTable), BorderLayout.CENTER);

        JPanel buttons = buttonPanel();

        JButton add = new JButton("Add Reaction");
        JButton delete = new JButton("Delete Reaction");
        JButton view = new JButton("View Reactions");

        add.addActionListener(e -> addReaction());
        delete.addActionListener(e -> deleteReaction());
        view.addActionListener(e -> viewReactions());

        buttons.add(add);
        buttons.add(delete);
        buttons.add(view);

        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createTagsPanel() {

        JPanel panel = interactionPanel();

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Username to tag:"));

        taggedUserInput = new JTextField(10);
        form.add(taggedUserInput);

        panel.add(form, BorderLayout.NORTH);

        tagsTable = new JTable();
        panel.add(new JScrollPane(tagsTable), BorderLayout.CENTER);

        JPanel buttons = buttonPanel();

        JButton add = new JButton("Add Tag");
        JButton delete = new JButton("Remove Tag");
        JButton view = new JButton("View Tags");

        add.addActionListener(e -> addTag());
        delete.addActionListener(e -> deleteTag());
        view.addActionListener(e -> viewTags());

        buttons.add(add);
        buttons.add(delete);
        buttons.add(view);

        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    // ---------- UI helpers ----------

    private JPanel interactionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(
            BorderFactory.createEmptyBorder(15, 10, 15, 10)
        );
        return panel;
    }

    private JPanel buttonPanel() {
        return new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
    }

    private JLabel whiteLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        return label;
    }

    private int selectedPostID() {
        PostItem item = (PostItem) postBox.getSelectedItem();

        if (item == null) {
            throw new IllegalArgumentException("Select a post first.");
        }
        return item.id;
    }

    private int positiveID(JTextField field) {
        int id = Integer.parseInt(field.getText().trim());
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be positive.");
        }
        return id;
    }

    private void message(String text) {
        JOptionPane.showMessageDialog(this, text);
    }

    private void error(Exception e) {
        String msg;
        if (e instanceof NumberFormatException) {
            msg = "Please enter a valid number.";
        } else if (e instanceof SQLIntegrityConstraintViolationException) {
            msg = "That user, post or RealMoji does not exist, " +
                  "or this entry already exists.";
        } else {
            msg = e.getMessage();
        }
        JOptionPane.showMessageDialog(
            this, msg, "Error", JOptionPane.ERROR_MESSAGE
        );
    }

    private void refreshAll() {
        if (postBox.getSelectedItem() == null) return;
        viewComments();
        viewReactions();
        viewTags();
    }

    // ---------- POST ----------

    private static class PostItem {
        int id;
        String caption;
        String image,username;
        Timestamp posted;
        PostItem(int id,String caption,String image,String username,Timestamp posted) {
            this.id=id;this.caption=caption;this.image=image;this.username=username;this.posted=posted;
        }
        @Override public String toString(){
            return "@"+username+" · "+posted+" · "+(caption==null?"":caption);
        }
    }

    private static final class RealMojiOption {
        final int id; final String name;
        RealMojiOption(int id,String name){this.id=id;this.name=name;}
        @Override public String toString(){return name;}
    }
    private void loadRealMojis(){
        realMojiBox.removeAllItems();
        try(Connection c=DBConnection.getConnection();PreparedStatement st=c.prepareStatement("SELECT RealMojiID,Emoji FROM REALMOJIS WHERE UserID=? ORDER BY Emoji")){
            st.setInt(1,currentUserID);try(ResultSet r=st.executeQuery()){
                while(r.next())realMojiBox.addItem(new RealMojiOption(r.getInt(1),r.getString(2)));
            }
        }catch(SQLException ex){error(ex);}
    }
    private int selectedRealMoji(){
        RealMojiOption item=(RealMojiOption)realMojiBox.getSelectedItem();
        if(item==null)throw new IllegalArgumentException("Create a RealMoji from the dashboard first, then refresh this list.");
        return item.id;
    }
    private void loadPosts() {

        String sql = "SELECT P.PostID,P.Caption,P.BackCam,P.PostedAt,U.Username FROM POST P " +
              "JOIN `USER` U ON U.UserID=P.UserID " +
              "WHERE COALESCE(U.AccountStatus,'Active')='Active' AND " +
              "(P.UserID=? OR U.Account_Type='Public' OR (EXISTS " +
              "(SELECT 1 FROM FRIENDSHIP F WHERE F.Status='Accepted' AND " +
              "((F.Requester_ID=? AND F.Receiver_ID=P.UserID) OR (F.Receiver_ID=? AND F.Requester_ID=P.UserID))) " +
              "AND EXISTS (SELECT 1 FROM POST OWN WHERE OWN.UserID=?))) " +
              "ORDER BY P.PostedAt DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            for(int i=1;i<=4;i++) st.setInt(i,currentUserID);
            try(ResultSet rs=st.executeQuery()){
            postBox.removeAllItems();

            while (rs.next()) {
                postBox.addItem(new PostItem(
                    rs.getInt("PostID"),
                    rs.getString("Caption"),
                    rs.getString("BackCam"),rs.getString("Username"),rs.getTimestamp("PostedAt")
                ));
            }
            }
            showSelectedPost();

        } catch (SQLException e) {
            error(e);
        }
    }

    private void showSelectedPost() {

        PostItem post = (PostItem) postBox.getSelectedItem();

        photoLabel.setIcon(null);

        if (post == null) {
            captionLabel.setText("No posts available.");
            photoLabel.setText("No post selected");
            return;
        }

        captionLabel.setText(
            post.caption == null ? "" : post.caption
        );

        photoLabel.setText("Post image unavailable");

        if (post.image != null) {
            File file = new File(post.image);

            if (file.exists()) {
                ImageIcon icon = new ImageIcon(file.getPath());

                Image scaled = icon.getImage().getScaledInstance(
                    420, 180, Image.SCALE_SMOOTH
                );

                photoLabel.setIcon(new ImageIcon(scaled));
                photoLabel.setText("");
            }
        }
    }

    // ---------- COMMENT ----------

    private void addComment() {

        try {
            int postID = selectedPostID();
            String content = commentInput.getText().trim();

            if (content.isEmpty()) {
                message("Enter a comment first.");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                if(!BeRealAccess.canInteract(con,currentUserID,postID))throw new SQLException("Only confirmed friends who have posted can comment.");
                // Obtain the next comment number for this post
                String nextSQL =
                    "SELECT COALESCE(MAX(CommentNumber), 0) + 1 " +
                    "FROM COMMENT WHERE PostID = ?";

                int number;

                try (PreparedStatement st = con.prepareStatement(nextSQL)) {
                    st.setInt(1, postID);

                    try (ResultSet rs = st.executeQuery()) {
                        rs.next();
                        number = rs.getInt(1);
                    }
                }

                String sql =
                    "INSERT INTO COMMENT " +
                    "(PostID, CommentNumber, Content, TimePosted, UserID) " +
                    "VALUES (?, ?, ?, NOW(), ?)";

                try (PreparedStatement st = con.prepareStatement(sql)) {
                    st.setInt(1, postID);
                    st.setInt(2, number);
                    st.setString(3, content);
                    st.setInt(4, currentUserID);
                    st.executeUpdate();
                }
            }

            commentInput.setText("");
            viewComments();
            message("Comment added successfully!");

        } catch (Exception e) {
            error(e);
        }
    }

    private void deleteComment() {

        try {
            int postID = selectedPostID();
            int row=commentsTable.getSelectedRow();
            if(row<0){message("Select your comment in the comments table first.");return;}
            int number=Integer.parseInt(String.valueOf(commentsTable.getValueAt(row,0)));

            String sql =
                "DELETE FROM COMMENT " +
                "WHERE PostID = ? AND CommentNumber = ? AND UserID = ?";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement st = con.prepareStatement(sql)) {

                st.setInt(1, postID);
                st.setInt(2, number);
                st.setInt(3, currentUserID);

                int rows = st.executeUpdate();
                message(rows > 0 ? "Comment deleted!" :
                        "Comment not found or not yours.");
            }

            viewComments();

        } catch (Exception e) {
            error(e);
        }
    }

    private void viewComments() {
        try {
            String sql =
                "SELECT CommentNumber, UserID, Content, TimePosted " +
                "FROM COMMENT WHERE PostID = ? " +
                "ORDER BY TimePosted ASC";

            showResults(sql, commentsTable, selectedPostID());

        } catch (Exception e) {
            error(e);
        }
    }

    // ---------- REACTIONS ----------

    private void addReaction() {
        try {
            int postID = selectedPostID();
            int realMojiID = selectedRealMoji();

            String sql =
                "INSERT INTO REACTS_WITH " +
                "(UserID, PostID, RealMojiID) VALUES (?, ?, ?)";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement st = con.prepareStatement(sql)) {
                if(!BeRealAccess.canInteract(con,currentUserID,postID))throw new SQLException("Only confirmed friends who have posted can react.");
                try(PreparedStatement owned=con.prepareStatement("SELECT COUNT(*) FROM REALMOJIS WHERE RealMojiID=? AND UserID=?")){
                    owned.setInt(1,realMojiID);owned.setInt(2,currentUserID);
                    try(ResultSet rs=owned.executeQuery()){rs.next();if(rs.getInt(1)==0)throw new SQLException("Choose a RealMoji you created.");}
                }
                st.setInt(1, currentUserID);
                st.setInt(2, postID);
                st.setInt(3, realMojiID);
                st.executeUpdate();
            }

            viewReactions();
            message("Reaction added!");

        } catch (Exception e) {
            error(e);
        }
    }

    private void deleteReaction() {
        try {
            int postID = selectedPostID();
            int realMojiID = selectedRealMoji();

            String sql =
                "DELETE FROM REACTS_WITH " +
                "WHERE UserID = ? AND PostID = ? AND RealMojiID = ?";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement st = con.prepareStatement(sql)) {

                st.setInt(1, currentUserID);
                st.setInt(2, postID);
                st.setInt(3, realMojiID);

                int rows = st.executeUpdate();
                message(rows > 0 ? "Reaction removed!" :
                        "Reaction not found.");
            }

            viewReactions();

        } catch (Exception e) {
            error(e);
        }
    }

    private void viewReactions() {
        try {
            String sql =
                "SELECT RW.UserID, RW.RealMojiID, R.Emoji " +
                "FROM REACTS_WITH RW " +
                "JOIN REALMOJIS R ON RW.RealMojiID = R.RealMojiID " +
                "WHERE RW.PostID = ?";

            showResults(sql, reactionsTable, selectedPostID());

        } catch (Exception e) {
            error(e);
        }
    }

    // ---------- TAGS ----------

    private void addTag() {
        try {
            int postID = selectedPostID();
            int taggedID;
            try(Connection c=DBConnection.getConnection()){ taggedID=BeRealAccess.userId(c,taggedUserInput.getText().trim()); }

            // Only the post owner can add tags
            if (!ownsPost(postID)) {
                message("You can only tag users in your own posts.");
                return;
            }

            String sql =
                "INSERT INTO TAGGED_ON (UserID, PostID) VALUES (?, ?)";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement st = con.prepareStatement(sql)) {

                st.setInt(1, taggedID);
                st.setInt(2, postID);
                st.executeUpdate();
            }

            viewTags();
            message("User tagged!");

        } catch (Exception e) {
            error(e);
        }
    }

    private void deleteTag() {
        try {
            int postID = selectedPostID();
            int taggedID;
            try(Connection c=DBConnection.getConnection()){ taggedID=BeRealAccess.userId(c,taggedUserInput.getText().trim()); }

            if (!ownsPost(postID)) {
                message("You can only remove tags from your own posts.");
                return;
            }

            String sql =
                "DELETE FROM TAGGED_ON WHERE UserID = ? AND PostID = ?";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement st = con.prepareStatement(sql)) {

                st.setInt(1, taggedID);
                st.setInt(2, postID);

                int rows = st.executeUpdate();
                message(rows > 0 ? "Tag removed!" : "Tag not found.");
            }

            viewTags();

        } catch (Exception e) {
            error(e);
        }
    }

    private boolean ownsPost(int postID) throws SQLException {

        String sql =
            "SELECT COUNT(*) FROM POST WHERE PostID = ? AND UserID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setInt(1, postID);
            st.setInt(2, currentUserID);

            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private void viewTags() {
        try {
            String sql =
                "SELECT T.UserID, U.Username " +
                "FROM TAGGED_ON T " +
                "JOIN `USER` U ON T.UserID = U.UserID " +
                "WHERE T.PostID = ?";

            showResults(sql, tagsTable, selectedPostID());

        } catch (Exception e) {
            error(e);
        }
    }

    // ---------- GENERAL RESULT TABLE ----------

    private void showResults(String sql, JTable table, int postID)
            throws SQLException {

        try (Connection con = DBConnection.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setInt(1, postID);

            try (ResultSet rs = st.executeQuery()) {

                ResultSetMetaData meta = rs.getMetaData();
                int count = meta.getColumnCount();

                String[] columns = new String[count];

                for (int i = 1; i <= count; i++) {
                    columns[i - 1] = meta.getColumnLabel(i);
                }

                DefaultTableModel model =
                    new DefaultTableModel(columns, 0) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };

                while (rs.next()) {
                    Object[] row = new Object[count];

                    for (int i = 1; i <= count; i++) {
                        row[i - 1] = rs.getObject(i);
                    }

                    model.addRow(row);
                }

                table.setModel(model);
            }
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            String input = JOptionPane.showInputDialog(
                null, "Enter your test User ID:"
            );

            if (input == null) return;

            try {
                int userID = Integer.parseInt(input.trim());

                if (userID <= 0) throw new NumberFormatException();

                new InteractionsGUI(userID).setVisible(true);

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                    null, "Enter a valid positive User ID."
                );
            }
        });
    }
}
