import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Pattern;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class UserManagementGUI extends JFrame {
    private static final Color BLACK = new Color(24, 24, 24);
    private static final Color GRAY = new Color(110, 110, 110);
    private static final Color LIGHT = new Color(246, 246, 246);
    private static final Color LINE = new Color(220, 220, 220);
    private final CardLayout cards = new CardLayout();
    private final JPanel screens = new JPanel(cards);
    private Integer createdID;

    public UserManagementGUI() {
        setTitle("BeReal. | User Management");
        setSize(860, 740);
        setMinimumSize(new Dimension(700, 610));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(screens);
        dashboard();
    }

    private JPanel page(String heading, String subtitle) {
        JPanel p = new JPanel(new BorderLayout(0, 22));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(28, 42, 28, 42));
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("BeReal.");
        brand.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel title = new JLabel(heading);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        header.add(brand);
        header.add(Box.createVerticalStrut(14));
        header.add(title);
        if (!subtitle.isEmpty()) {
            JLabel note = new JLabel(subtitle);
            note.setForeground(GRAY);
            note.setFont(new Font("SansSerif", Font.PLAIN, 13));
            header.add(Box.createVerticalStrut(6));
            header.add(note);
        }
        p.add(header, BorderLayout.NORTH);
        return p;
    }

    private void show(String key, JPanel page) {
        screens.add(page, key);
        cards.show(screens, key);
        screens.revalidate();
        screens.repaint();
    }

    private JButton button(String text, boolean primary) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setForeground(primary ? Color.WHITE : BLACK);
        b.setBackground(primary ? BLACK : LIGHT);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new CompoundBorder(new LineBorder(primary ? BLACK : LINE, 1, true),
                new EmptyBorder(12, 18, 12, 18)));
        return b;
    }

    private JTextField input() {
        JTextField f = new JTextField();
        style(f);
        return f;
    }

    private void style(JComponent c) {
        c.setFont(new Font("SansSerif", Font.PLAIN, 14));
        c.setBackground(Color.WHITE);
        c.setForeground(BLACK);
        c.setBorder(new CompoundBorder(new LineBorder(LINE, 1, true),
                new EmptyBorder(11, 12, 11, 12)));
    }

    private JPanel form() {
        JPanel f = new JPanel(new GridBagLayout());
        f.setOpaque(false);
        return f;
    }

    private void addField(JPanel f, String name, JComponent component, int row) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.35;
        c.insets = new Insets(9, 5, 9, 15);
        JLabel label = new JLabel(name);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        f.add(label, c);
        c.gridx = 1;
        c.weightx = 0.65;
        c.insets = new Insets(9, 0, 9, 5);
        f.add(component, c);
    }

    private JPanel footer(JButton action) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p.setOpaque(false);
        JButton back = button("Back", false);
        back.addActionListener(e -> dashboard());
        p.add(back);
        if (action != null) p.add(action);
        return p;
    }

    private void error(String text) {
        JOptionPane.showMessageDialog(this, text, "Please check", JOptionPane.ERROR_MESSAGE);
    }

    private void message(String text) {
        JOptionPane.showMessageDialog(this, text, "BeReal.", JOptionPane.INFORMATION_MESSAGE);
    }

    private Integer id(String text) {
        try {
            int number = Integer.parseInt(text.trim());
            return number > 0 ? number : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean validEmail(String value) {
        return Pattern.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", value);
    }

    private String capitalized(String value) {
        if (value == null || value.trim().isEmpty()) return "Not available";
        String v = value.trim();
        return v.substring(0, 1).toUpperCase() + v.substring(1);
    }

    private JLabel value(String text) {
        JLabel l = new JLabel(text == null || text.trim().isEmpty() ? "—" : text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return l;
    }

    private String hashPassword(char[] password) throws Exception {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        PBEKeySpec spec = new PBEKeySpec(password, salt, 120000, 256);
        try {
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
            return "pbkdf2:120000:" + Base64.getEncoder().encodeToString(salt)
                    + ":" + Base64.getEncoder().encodeToString(hash);
        } finally {
            spec.clearPassword();
        }
    }

    private JTextField dateInput() {
        JTextField f = input();
        ((AbstractDocument) f.getDocument()).setDocumentFilter(new DocumentFilter() {
            private void change(FilterBypass fb, int offset, int length, String text)
                    throws BadLocationException {
                String old = fb.getDocument().getText(0, fb.getDocument().getLength());
                StringBuilder next = new StringBuilder(old);
                next.replace(offset, offset + length, text == null ? "" : text);
                String digits = next.toString().replaceAll("[^0-9]", "");
                if (digits.length() > 8) digits = digits.substring(0, 8);
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digits.length(); i++) {
                    if (i == 4 || i == 6) formatted.append('-');
                    formatted.append(digits.charAt(i));
                }
                fb.replace(0, fb.getDocument().getLength(), formatted.toString(), null);
            }
            @Override public void insertString(FilterBypass fb, int offset, String text, AttributeSet a)
                    throws BadLocationException { change(fb, offset, 0, text); }
            @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet a)
                    throws BadLocationException { change(fb, offset, length, text); }
            @Override public void remove(FilterBypass fb, int offset, int length)
                    throws BadLocationException { change(fb, offset, length, ""); }
        });
        f.setToolTipText("Date format: YYYY-MM-DD (hyphens added automatically)");
        return f;
    }

    private static class Avatar extends JComponent {
        private String path = "";
        Avatar(int size) { setPreferredSize(new Dimension(size, size)); }
        void setPhoto(String photo) { path = photo == null ? "" : photo; repaint(); }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int d = Math.min(getWidth(), getHeight()) - 4;
            int x = (getWidth() - d) / 2, y = (getHeight() - d) / 2;
            g.setColor(new Color(232, 232, 232));
            g.fillOval(x, y, d, d);
            boolean drawn = false;
            if (!path.isEmpty()) {
                try {
                    BufferedImage img = javax.imageio.ImageIO.read(new File(path));
                    if (img != null) {
                        g.setClip(new Ellipse2D.Double(x, y, d, d));
                        double scale = Math.max((double) d / img.getWidth(), (double) d / img.getHeight());
                        int w = (int) Math.ceil(img.getWidth() * scale);
                        int h = (int) Math.ceil(img.getHeight() * scale);
                        g.drawImage(img, x + (d - w) / 2, y + (d - h) / 2, w, h, null);
                        g.setClip(null);
                        drawn = true;
                    }
                } catch (Exception ignored) { }
            }
            if (!drawn) {
                g.setColor(new Color(135, 135, 135));
                int head = (int) (d * .29);
                g.fillOval(x + (d - head) / 2, y + (int) (d * .18), head, head);
                g.setClip(new Ellipse2D.Double(x, y, d, d));
                int shoulders = (int) (d * .65);
                g.fillRoundRect(x + (d - shoulders) / 2, y + (int) (d * .57),
                        shoulders, (int) (d * .46), shoulders, shoulders);
            }
            g.dispose();
        }
    }

    private void dashboard() {
        JPanel p = page("User Management", "Manage your accounts.");
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setPreferredSize(new Dimension(410, 265));
        JButton register = button("Register User", true);
        JButton update = button("Update User", false);
        JButton retrieve = button("View User", false);
        for (JButton b : new JButton[]{register, update, retrieve}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setMaximumSize(new Dimension(410, 54));
            menu.add(b);
            menu.add(Box.createVerticalStrut(17));
        }
        register.addActionListener(e -> registration());
        update.addActionListener(e -> findUser(true));
        retrieve.addActionListener(e -> findUser(false));
        center.add(menu);
        p.add(center, BorderLayout.CENTER);
        show("dashboard", p);
    }

    private void registration() {
        JPanel p = page("Register User", "Create your account.");
        JPanel f = form();
        JTextField email = input(), username = input(), bio = input(), dob = dateInput();
        JPasswordField password = new JPasswordField();
        style(password);
        JComboBox<String> privacy = new JComboBox<>(new String[]{"Public", "Private"});
        privacy.setFont(new Font("SansSerif", Font.PLAIN, 14));
        privacy.setBackground(Color.WHITE);
        JPanel privacyBox = new JPanel(new BorderLayout());
        privacyBox.setOpaque(false);
        privacyBox.setPreferredSize(new Dimension(260, 46));
        privacyBox.add(privacy);
        Avatar avatar = new Avatar(88);
        String[] photoPath = {""};
        JButton choose = button("Choose photo", false), remove = button("Remove", false);
        JPanel photo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        photo.setOpaque(false);
        photo.add(avatar);
        photo.add(choose);
        photo.add(remove);
        choose.addActionListener(e -> {
            JFileChooser picker = new JFileChooser();
            picker.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Images", "png", "jpg", "jpeg"));
            if (picker.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = picker.getSelectedFile();
                try {
                    if (javax.imageio.ImageIO.read(file) == null) {
                        error("Choose a valid image.");
                        return;
                    }
                    if (file.getAbsolutePath().length() > 255) {
                        error("Choose an image with a shorter file path.");
                        return;
                    }
                    photoPath[0] = file.getAbsolutePath();
                    avatar.setPhoto(photoPath[0]);
                } catch (Exception ex) { error("Unable to open the selected photo."); }
            }
        });
        remove.addActionListener(e -> { photoPath[0] = ""; avatar.setPhoto(""); });
        addField(f, "Profile photo", photo, 0);
        addField(f, "Email *", email, 1);
        addField(f, "Username *", username, 2);
        addField(f, "Password *", password, 3);
        addField(f, "Bio", bio, 4);
        addField(f, "Date of birth (YYYY-MM-DD)", dob, 5);
        addField(f, "Account privacy", privacyBox, 6);
        JButton save = button("Create account", true);
        save.addActionListener(e -> {
            String mail = email.getText().trim(), name = username.getText().trim();
            String date = dob.getText().trim();
            char[] pass = password.getPassword();
            try {
                if (mail.isEmpty() || name.isEmpty() || pass.length == 0) {
                    error("Email, username, and password are required.");
                    return;
                }
                if (!validEmail(mail) || mail.length() > 255) {
                    error("Enter a valid email address.");
                    return;
                }
                if (name.length() > 50) { error("Username must be 50 characters or fewer."); return; }
                if (!date.isEmpty()) {
                    try {
                        if (date.length() != 10 || LocalDate.parse(date).isAfter(LocalDate.now()))
                            throw new IllegalArgumentException();
                    } catch (Exception ex) { error("Enter a valid birth date in YYYY-MM-DD format."); return; }
                }
                String sql = "INSERT INTO `USER` (Email, Username, Password, Bio, DOB, "
                        + "Account_Type, UserType, ProfilePicture, AccountStatus, JoinDate) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement st = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    st.setString(1, mail);
                    st.setString(2, name);
                    st.setString(3, hashPassword(pass));
                    st.setString(4, bio.getText().trim());
                    if (date.isEmpty()) st.setNull(5, Types.DATE);
                    else st.setDate(5, java.sql.Date.valueOf(date));
                    st.setString(6, privacy.getSelectedItem().toString());
                    st.setString(7, "regular");
                    st.setString(8, photoPath[0]);
                    st.setString(9, "active");
                    st.setDate(10, java.sql.Date.valueOf(LocalDate.now()));
                    st.executeUpdate();
                    try (ResultSet keys = st.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Could not retrieve User ID.");
                        createdID = keys.getInt(1);
                    }
                    confirmation();
                } catch (SQLException ex) { error("Registration failed: " + ex.getMessage()); }
            } catch (Exception ex) { error("Unable to create account: " + ex.getMessage()); }
            finally { Arrays.fill(pass, '\0'); }
        });
        JScrollPane scroll = new JScrollPane(f);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        p.add(scroll, BorderLayout.CENTER);
        p.add(footer(save), BorderLayout.SOUTH);
        show("register", p);
    }

    private void confirmation() {
        if (createdID == null) { dashboard(); return; }
        JPanel p = page("Account created!", "Your account is ready to use.");
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        JPanel details = form();
        details.setPreferredSize(new Dimension(450, 345));
        try (Connection con = DBConnection.getConnection();
             PreparedStatement st = con.prepareStatement("SELECT * FROM `USER` WHERE UserID = ?")) {
            st.setInt(1, createdID);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    addField(details, "User ID", value(rs.getString("UserID")), 0);
                    addField(details, "Username", value("@" + rs.getString("Username")), 1);
                    addField(details, "Email", value(rs.getString("Email")), 2);
                    addField(details, "Privacy", value(capitalized(rs.getString("Account_Type"))), 3);
                    addField(details, "User type", value(capitalized(rs.getString("UserType"))), 4);
                    addField(details, "Status", value(capitalized(rs.getString("AccountStatus"))), 5);
                    addField(details, "Joined", value(rs.getString("JoinDate")), 6);
                }
            }
        } catch (SQLException ex) { error("Unable to load account details: " + ex.getMessage()); }
        center.add(details);
        p.add(center, BorderLayout.CENTER);
        JButton view = button("View profile", true);
        view.addActionListener(e -> profile(createdID));
        p.add(footer(view), BorderLayout.SOUTH);
        show("confirmation", p);
    }

    private void findUser(boolean edit) {
        JPanel p = page(edit ? "Update User" : "View User",
                "Find an account using its username, email, or User ID.");
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        JPanel searchPanel = new JPanel();
        searchPanel.setOpaque(false);
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setPreferredSize(new Dimension(470, 360));

        JLabel prompt = new JLabel("Username, email, or User ID");
        prompt.setFont(new Font("SansSerif", Font.BOLD, 13));
        prompt.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField searchText = input();
        searchText.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchText.setMinimumSize(new Dimension(470, 46));
        searchText.setMaximumSize(new Dimension(470, 46));
        searchText.setPreferredSize(new Dimension(470, 46));
        JButton search = button("Search", true);
        search.setAlignmentX(Component.LEFT_ALIGNMENT);
        search.setMinimumSize(new Dimension(470, 48));
        search.setPreferredSize(new Dimension(470, 48));
        search.setMaximumSize(new Dimension(470, 48));

        searchPanel.add(prompt);
        searchPanel.add(Box.createVerticalStrut(10));
        searchPanel.add(searchText);
        searchPanel.add(Box.createVerticalStrut(12));
        searchPanel.add(search);
        searchPanel.add(Box.createVerticalStrut(22));

        JLabel existing = new JLabel("Or choose an account");
        existing.setFont(new Font("SansSerif", Font.BOLD, 13));
        existing.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchPanel.add(existing);
        searchPanel.add(Box.createVerticalStrut(10));

        DefaultListModel<UserChoice> model = new DefaultListModel<>();
        JList<UserChoice> accounts = new JList<>(model);
        accounts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accounts.setFont(new Font("SansSerif", Font.PLAIN, 14));
        accounts.setFixedCellHeight(35);
        JScrollPane listScroll = new JScrollPane(accounts);
        listScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        listScroll.setMinimumSize(new Dimension(470, 150));
        listScroll.setPreferredSize(new Dimension(470, 150));
        listScroll.setMaximumSize(new Dimension(470, 150));
        listScroll.setBorder(new LineBorder(LINE));
        searchPanel.add(listScroll);

        JButton open = button(edit ? "Edit selected user" : "View selected user", false);
        open.setAlignmentX(Component.LEFT_ALIGNMENT);
        open.setMinimumSize(new Dimension(470, 48));
        open.setPreferredSize(new Dimension(470, 48));
        open.setMaximumSize(new Dimension(470, 48));
        searchPanel.add(Box.createVerticalStrut(12));
        searchPanel.add(open);

        try (Connection con = DBConnection.getConnection();
             PreparedStatement st = con.prepareStatement(
                     "SELECT UserID, Username FROM `USER` ORDER BY Username, UserID");
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                model.addElement(new UserChoice(rs.getInt("UserID"), rs.getString("Username")));
            }
        } catch (SQLException ex) {
            error("Unable to load accounts: " + ex.getMessage());
        }

        Runnable searchAction = () -> {
            String query = searchText.getText().trim();
            if (query.isEmpty()) {
                error("Enter a username, email, or User ID.");
                return;
            }
            String sql = "SELECT UserID FROM `USER` WHERE Username = ? OR Email = ?";
            Integer number = id(query);
            if (number != null) sql += " OR UserID = ?";
            sql += " ORDER BY UserID LIMIT 1";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement st = con.prepareStatement(sql)) {
                st.setString(1, query);
                st.setString(2, query);
                if (number != null) st.setInt(3, number);
                try (ResultSet rs = st.executeQuery()) {
                    if (!rs.next()) {
                        error("No account found. Check your search and try again.");
                        return;
                    }
                    int foundID = rs.getInt("UserID");
                    if (edit) editUser(foundID);
                    else profile(foundID);
                }
            } catch (SQLException ex) {
                error("Search failed: " + ex.getMessage());
            }
        };
        search.addActionListener(e -> searchAction.run());
        searchText.addActionListener(e -> searchAction.run());
        open.addActionListener(e -> {
            UserChoice selected = accounts.getSelectedValue();
            if (selected == null) {
                error("Choose an account from the list first.");
                return;
            }
            if (edit) editUser(selected.userID);
            else profile(selected.userID);
        });
        accounts.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) open.doClick();
            }
        });

        center.add(searchPanel);
        p.add(center, BorderLayout.CENTER);
        p.add(footer(null), BorderLayout.SOUTH);
        show(edit ? "findEdit" : "findView", p);
    }

    private static class UserChoice {
        final int userID;
        final String username;
        UserChoice(int userID, String username) {
            this.userID = userID;
            this.username = username;
        }
        @Override public String toString() {
            return "@" + username + "   ·   ID " + userID;
        }
    }

    private void profile(int userID) {
        JPanel p = page("User Profile", "Account information");
        JPanel card = new JPanel(new BorderLayout(0, 18));
        card.setBackground(LIGHT);
        card.setBorder(new EmptyBorder(25, 25, 25, 25));
        try (Connection con = DBConnection.getConnection();
             PreparedStatement st = con.prepareStatement("SELECT * FROM `USER` WHERE UserID = ?")) {
            st.setInt(1, userID);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) { error("User ID not found."); return; }
                JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
                top.setOpaque(false);
                Avatar avatar = new Avatar(105);
                avatar.setPhoto(rs.getString("ProfilePicture"));
                top.add(avatar);
                JPanel identity = new JPanel(new GridLayout(3, 1, 0, 3));
                identity.setOpaque(false);
                JLabel name = value("@" + rs.getString("Username"));
                name.setFont(new Font("SansSerif", Font.BOLD, 24));
                identity.add(name);
                identity.add(value("User ID: " + userID));
                identity.add(value("Status: " + capitalized(rs.getString("AccountStatus"))));
                top.add(identity);
                card.add(top, BorderLayout.NORTH);
                JPanel info = form();
                addField(info, "Email", value(rs.getString("Email")), 0);
                addField(info, "Bio", value(rs.getString("Bio")), 1);
                addField(info, "Date of birth", value(rs.getString("DOB")), 2);
                addField(info, "Privacy", value(capitalized(rs.getString("Account_Type"))), 3);
                addField(info, "User type", value(capitalized(rs.getString("UserType"))), 4);
                addField(info, "Joined", value(rs.getString("JoinDate")), 5);
                card.add(info, BorderLayout.CENTER);
            }
        } catch (SQLException ex) { error("Unable to load profile: " + ex.getMessage()); return; }
        JScrollPane scroll = new JScrollPane(card);
        scroll.setBorder(null);
        p.add(scroll, BorderLayout.CENTER);
        JButton edit = button("Edit user", true);
        edit.addActionListener(e -> editUser(userID));
        p.add(footer(edit), BorderLayout.SOUTH);
        show("profile", p);
    }

    private void editUser(int userID) {
        JPanel p = page("Update User", "Edit account information.");
        JPanel f = form();
        JTextField email = input(), username = input(), bio = input(), dob = dateInput();
        JComboBox<String> privacy = new JComboBox<>(new String[]{"Public", "Private"});
        privacy.setFont(new Font("SansSerif", Font.PLAIN, 14));
        privacy.setBackground(Color.WHITE);
        JPanel privacyBox = new JPanel(new BorderLayout());
        privacyBox.setOpaque(false);
        privacyBox.setPreferredSize(new Dimension(260, 46));
        privacyBox.add(privacy);
        Avatar avatar = new Avatar(88);
        String[] photoPath = {""};
        try (Connection con = DBConnection.getConnection();
             PreparedStatement st = con.prepareStatement("SELECT * FROM `USER` WHERE UserID = ?")) {
            st.setInt(1, userID);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) { error("User ID not found."); return; }
                email.setText(rs.getString("Email"));
                username.setText(rs.getString("Username"));
                bio.setText(rs.getString("Bio"));
                String date = rs.getString("DOB");
                if (date != null) dob.setText(date);
                privacy.setSelectedItem(capitalized(rs.getString("Account_Type")));
                photoPath[0] = rs.getString("ProfilePicture");
                avatar.setPhoto(photoPath[0]);
            }
        } catch (SQLException ex) { error("Unable to load user: " + ex.getMessage()); return; }
        JButton choose = button("Choose photo", false), remove = button("Remove", false);
        JPanel photo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        photo.setOpaque(false);
        photo.add(avatar);
        photo.add(choose);
        photo.add(remove);
        choose.addActionListener(e -> {
            JFileChooser picker = new JFileChooser();
            picker.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Images", "png", "jpg", "jpeg"));
            if (picker.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = picker.getSelectedFile();
                try {
                    if (javax.imageio.ImageIO.read(file) == null) { error("Choose a valid image."); return; }
                    if (file.getAbsolutePath().length() > 255) { error("Choose an image with a shorter file path."); return; }
                    photoPath[0] = file.getAbsolutePath();
                    avatar.setPhoto(photoPath[0]);
                } catch (Exception ex) { error("Unable to open the selected photo."); }
            }
        });
        remove.addActionListener(e -> { photoPath[0] = ""; avatar.setPhoto(""); });
        addField(f, "User ID", value(String.valueOf(userID)), 0);
        addField(f, "Profile photo", photo, 1);
        addField(f, "Email", email, 2);
        addField(f, "Username", username, 3);
        addField(f, "Bio", bio, 4);
        addField(f, "Date of birth (YYYY-MM-DD)", dob, 5);
        addField(f, "Account privacy", privacyBox, 6);
        JButton save = button("Save changes", true);
        save.addActionListener(e -> {
            String mail = email.getText().trim(), name = username.getText().trim();
            String date = dob.getText().trim();
            if (!validEmail(mail) || mail.length() > 255) { error("Enter a valid email address."); return; }
            if (name.isEmpty() || name.length() > 50) { error("Username must be 1–50 characters."); return; }
            if (!date.isEmpty()) {
                try {
                    if (date.length() != 10 || LocalDate.parse(date).isAfter(LocalDate.now()))
                        throw new IllegalArgumentException();
                } catch (Exception ex) { error("Enter a valid birth date in YYYY-MM-DD format."); return; }
            }
            String sql = "UPDATE `USER` SET Email = ?, Username = ?, Bio = ?, DOB = ?, "
                    + "Account_Type = ?, ProfilePicture = ? WHERE UserID = ?";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement st = con.prepareStatement(sql)) {
                st.setString(1, mail);
                st.setString(2, name);
                st.setString(3, bio.getText().trim());
                if (date.isEmpty()) st.setNull(4, Types.DATE);
                else st.setDate(4, java.sql.Date.valueOf(date));
                st.setString(5, privacy.getSelectedItem().toString());
                st.setString(6, photoPath[0]);
                st.setInt(7, userID);
                if (st.executeUpdate() > 0) { message("Changes saved!"); profile(userID); }
                else error("User ID not found.");
            } catch (SQLException ex) { error("Update failed: " + ex.getMessage()); }
        });
        JScrollPane scroll = new JScrollPane(f);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        p.add(scroll, BorderLayout.CENTER);
        p.add(footer(save), BorderLayout.SOUTH);
        show("edit", p);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserManagementGUI().setVisible(true));
    }
}
