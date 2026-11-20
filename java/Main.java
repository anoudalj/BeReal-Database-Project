import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;
import java.util.Arrays;

public class Main extends JFrame {
    // Demo-only authorization password. Change this before sharing the project.
    // This is not secure against anyone who can inspect the application code.
    private static final String ADMIN_CREATION_PASSWORD = "admin6";
    private int failedAdminAttempts = 0;
    private final JPanel content = BeRealTheme.panel(new BorderLayout());
    public Main() {
        BeRealTheme.install(); setTitle("BeReal."); setSize(540, 860);
        setMinimumSize(new Dimension(480, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setLocationRelativeTo(null);
        setContentPane(content); showWelcome();
    }
    private JPanel page(String title, String subtitle) {
        JPanel p=BeRealTheme.panel(new BorderLayout(0,28));
        p.setBorder(BorderFactory.createEmptyBorder(32,30,26,30));
        p.add(BeRealTheme.header(""),BorderLayout.NORTH);
        JPanel center=BeRealTheme.panel(new GridBagLayout());
        JPanel stack=BeRealTheme.panel(null); stack.setLayout(new BoxLayout(stack,BoxLayout.Y_AXIS));
        JLabel h=BeRealTheme.label(title,28,true); h.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub=BeRealTheme.note(subtitle); sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        stack.add(h);stack.add(Box.createVerticalStrut(12));stack.add(sub);stack.add(Box.createVerticalStrut(36));
        center.add(stack);p.add(center,BorderLayout.CENTER);
        p.putClientProperty("stack",stack);return p;
    }
    private JPanel stack(JPanel p) { return (JPanel)p.getClientProperty("stack"); }
    private void addButton(JPanel p,JButton b) {
        b.setAlignmentX(Component.CENTER_ALIGNMENT);b.setMaximumSize(new Dimension(380,50));
        stack(p).add(b);stack(p).add(Box.createVerticalStrut(12));
    }
    private void field(JPanel p,String name,JComponent f) {
        JLabel l=BeRealTheme.note(name);l.setAlignmentX(Component.CENTER_ALIGNMENT);stack(p).add(l);
        stack(p).add(Box.createVerticalStrut(7));
        f.setMaximumSize(new Dimension(380,48));f.setAlignmentX(Component.CENTER_ALIGNMENT);
        if(f instanceof javax.swing.text.JTextComponent) BeRealTheme.input((javax.swing.text.JTextComponent)f);
        stack(p).add(f);stack(p).add(Box.createVerticalStrut(18));
    }
    private void showWelcome() {
        JPanel p=page("Your friends. For real.","A little moment from your everyday life.");
        JLabel mark=BeRealTheme.label("BeReal.",54,true);mark.setAlignmentX(Component.CENTER_ALIGNMENT);
        stack(p).add(mark,0);stack(p).add(Box.createVerticalStrut(56),1);
        JButton create=BeRealTheme.button("Create an account",true),login=BeRealTheme.button("Log in",false);
        create.addActionListener(e -> new UserManagementGUI().setVisible(true));
        login.addActionListener(e -> showLogin());addButton(p,create);addButton(p,login);
        JButton admin=BeRealTheme.button("Create admin account",false);
        admin.addActionListener(e -> showAdminGate());
        addButton(p,admin);
        showPage(p);
    }

    // Step 1: Require the separate admin creation password BEFORE showing registration.
    private void showAdminGate() {
        JPanel p=page("Admin authorization", "Enter the admin creation password to continue.");
        JPasswordField key=new JPasswordField(22);
        field(p,"Admin creation password",key);
        JButton unlock=BeRealTheme.button("Continue to admin registration",true);
        JButton back=BeRealTheme.button("Back",false);
        unlock.addActionListener(e -> {
            char[] supplied=key.getPassword();
            try {
                if (failedAdminAttempts >= 5) {
                    error("Too many incorrect attempts. Restart the app to try again.");
                    return;
                }
                char[] expected=ADMIN_CREATION_PASSWORD.toCharArray();
                boolean valid=MessageDigest.isEqual(
                    new String(supplied).getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    ADMIN_CREATION_PASSWORD.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                Arrays.fill(expected,'\0');
                if (!valid) {
                    failedAdminAttempts++;
                    error("Incorrect admin creation password.");
                    key.setText("");
                    return;
                }
                failedAdminAttempts=0;
                showAdminRegistration();
            } finally { Arrays.fill(supplied,'\0'); }
        });
        key.addActionListener(e -> unlock.doClick());
        back.addActionListener(e -> showWelcome());
        addButton(p,unlock);addButton(p,back);
        showPage(p);getRootPane().setDefaultButton(unlock);
    }

    // Step 2: Only accessible after successful authorization.
    private void showAdminRegistration() {
        JPanel p=page("Create admin account", "Register a new administrator account.");
        JTextField username=new JTextField(22), email=new JTextField(22);
        JPasswordField password=new JPasswordField(22), confirm=new JPasswordField(22);
        field(p,"Admin username",username);
        field(p,"Admin email",email);
        field(p,"New account password",password);
        field(p,"Confirm account password",confirm);
        JButton create=BeRealTheme.button("Create admin",true),back=BeRealTheme.button("Back",false);
        create.addActionListener(e -> createAdmin(username.getText(),email.getText(),password.getPassword(),confirm.getPassword()));
        back.addActionListener(e -> showWelcome());
        addButton(p,create);addButton(p,back);showPage(p);
        getRootPane().setDefaultButton(create);
    }
    private static String hashNewPassword(char[] password) throws Exception {
        byte[] salt=new byte[16]; new SecureRandom().nextBytes(salt);
        PBEKeySpec spec=new PBEKeySpec(password,salt,120000,256);
        try {
            byte[] hash=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return "pbkdf2:120000:"+Base64.getEncoder().encodeToString(salt)+":"+Base64.getEncoder().encodeToString(hash);
        } finally { spec.clearPassword(); }
    }
    private void createAdmin(String rawUsername,String rawEmail,char[] password,char[] confirmation) {
        try {
            String username=rawUsername.trim(),email=rawEmail.trim();
            if(username.isEmpty() || username.length()>50) { error("Enter a username of up to 50 characters."); return; }
            if(email.length()>255 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                error("Enter a valid email address."); return;
            }
            if(password.length<8) { error("Account password must have at least 8 characters."); return; }
            if(!Arrays.equals(password,confirmation)) { error("Account passwords do not match."); return; }
            int choice=JOptionPane.showConfirmDialog(this,"Create admin account @"+username+"?",
                    "Confirm admin account",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
            if(choice!=JOptionPane.YES_OPTION) return;
            String sql="INSERT INTO `USER` (Email, Username, Password, Bio, DOB, Account_Type, UserType, ProfilePicture, AccountStatus, JoinDate) "
                    +"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try(Connection c=DBConnection.getConnection();PreparedStatement st=c.prepareStatement(sql)) {
                st.setString(1,email); st.setString(2,username); st.setString(3,hashNewPassword(password));
                st.setString(4,""); st.setNull(5,Types.DATE); st.setString(6,"Private");
                st.setString(7,"Admin"); st.setNull(8,Types.VARCHAR); st.setString(9,"Active");
                st.setDate(10,java.sql.Date.valueOf(java.time.LocalDate.now()));
                if(st.executeUpdate()!=1) throw new SQLException("Account creation failed.");
            }
            JOptionPane.showMessageDialog(this,"Admin @"+username+" created. Log in using the Admin role and your new account password.");
            showLogin();
        } catch(SQLException ex) {
            if("23000".equals(ex.getSQLState())) error("Username or email already exists.");
            else error("Database error: "+ex.getMessage());
        } catch(Exception ex) { error("Could not create admin account: "+ex.getMessage()); }
        finally { Arrays.fill(password,'\0'); Arrays.fill(confirmation,'\0'); }
    }
    private void showLogin() {
        JPanel p=page("Welcome back.","Log in to see what your friends are up to.");
        JTextField identity=new JTextField(22);JPasswordField password=new JPasswordField(22);
        JComboBox<String> role=new JComboBox<>(new String[]{"User","Admin"});
        field(p,"Username or email",identity);field(p,"Password",password);field(p,"Account role",role);
        JButton signIn=BeRealTheme.button("Continue",true),back=BeRealTheme.button("Back",false);
        signIn.addActionListener(e -> authenticate(identity.getText(),password.getPassword(),(String)role.getSelectedItem()));
        password.addActionListener(e -> signIn.doClick());back.addActionListener(e -> showWelcome());
        addButton(p,signIn);addButton(p,back);showPage(p);getRootPane().setDefaultButton(signIn);
    }
    private void authenticate(String identity, char[] password, String role) {
        if (identity.trim().isEmpty() || password.length == 0) { error("Enter your username/email and password."); return; }
        String sql = "SELECT UserID, Username, Password, UserType FROM `USER` WHERE Username = ? OR Email = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, identity.trim()); s.setString(2, identity.trim());
            try (ResultSet r = s.executeQuery()) {
                if (!r.next() || !roleMatches(role, r.getString("UserType")) || !verify(password, r.getString("Password"))) { error("Login failed. Check your credentials and selected role."); return; }
                dashboard(r.getInt("UserID"), r.getString("Username"), role);
            }
        } catch (SQLException ex) { error("Database error: " + ex.getMessage()); }
        finally { java.util.Arrays.fill(password, '\0'); }
    }
    // Supports passwords generated by UserManagementGUI: pbkdf2:iterations:base64Salt:base64Hash.
    private boolean verify(char[] password, String stored) {
        try {
            String[] parts = stored.split(":"); if (parts.length != 4 || !"pbkdf2".equals(parts[0])) return false;
            PBEKeySpec spec = new PBEKeySpec(password, Base64.getDecoder().decode(parts[2]), Integer.parseInt(parts[1]), 256);
            byte[] actual = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); spec.clearPassword();
            return MessageDigest.isEqual(actual, Base64.getDecoder().decode(parts[3]));
        } catch (Exception ex) { return false; }
    }
    // Existing projects commonly call a non-admin account either User or Regular.
    private boolean roleMatches(String selected, String stored) { return "Admin".equalsIgnoreCase(selected) ? "Admin".equalsIgnoreCase(stored) : ("User".equalsIgnoreCase(stored) || "Regular".equalsIgnoreCase(stored)); }
    private void dashboard(int userId,String username,String role) {
        getRootPane().setDefaultButton(null);
        JPanel p=BeRealTheme.panel(new BorderLayout(0,22));
        p.setBorder(BorderFactory.createEmptyBorder(22,24,20,24));
        JPanel header=BeRealTheme.panel(new BorderLayout(8,0));
        JButton friends=BeRealTheme.button("Friends",false),profile=BeRealTheme.button("Profile",false);
        friends.addActionListener(e -> new FriendshipManagement(userId).setVisible(true));
        profile.addActionListener(e -> new UserManagementGUI(userId).setVisible(true));
        header.add(friends,BorderLayout.WEST);header.add(BeRealTheme.header(""),BorderLayout.CENTER);header.add(profile,BorderLayout.EAST);
        p.add(header,BorderLayout.NORTH);BeRealFeed feed=new BeRealFeed(userId);p.add(feed,BorderLayout.CENTER);
        JPanel bottom=BeRealTheme.panel(new BorderLayout(0,12));
        JButton capture=BeRealTheme.button("Take a BeReal",true);
        capture.addActionListener(e -> { PostGUI editor=new PostGUI(userId);editor.addWindowListener(new java.awt.event.WindowAdapter(){
            @Override public void windowClosed(java.awt.event.WindowEvent event){feed.refresh();}
        });editor.setVisible(true); });bottom.add(capture,BorderLayout.NORTH);
        JPanel links=BeRealTheme.panel(new FlowLayout(FlowLayout.CENTER,8,0));
        if("Admin".equalsIgnoreCase(role)) {
            JButton manage=BeRealTheme.button("Manage users & posts",false);manage.addActionListener(e -> new AdminManagementGUI(userId).setVisible(true));links.add(manage);
            JButton queries=BeRealTheme.button("Advanced Queries",false);queries.addActionListener(e -> new AdvancedQueriesGUI().setVisible(true));links.add(queries);
        }
        JButton logout=BeRealTheme.button("Log out",false);logout.addActionListener(e -> showWelcome());links.add(logout);
        bottom.add(links,BorderLayout.CENTER);p.add(bottom,BorderLayout.SOUTH);showPage(p);
    }
    private void showPage(JPanel p) { content.removeAll();content.add(p);content.revalidate();content.repaint(); }
    private void error(String text) { JOptionPane.showMessageDialog(this,text,"BeReal.",JOptionPane.ERROR_MESSAGE); }
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> {BeRealTheme.install();new Main().setVisible(true);}); }
}
