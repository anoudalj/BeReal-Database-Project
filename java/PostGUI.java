import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.sql.*;

/** Post editor. FrontCam and BackCam retain the existing schema's path strings. */
public class PostGUI extends JFrame {
    private final int currentUserId;
    public PostGUI() { this(-1); }
    public PostGUI(int currentUserId) {
        BeRealTheme.install(); this.currentUserId = currentUserId;
        setTitle("BeReal. | Your moments"); setSize(560, 820);
        setMinimumSize(new Dimension(500, 680));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); setLocationRelativeTo(null);
        JPanel root=BeRealTheme.panel(new BorderLayout(0,20));
        root.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        root.add(BeRealTheme.header("Your moments, just as they are."),BorderLayout.NORTH);
        if(currentUserId>0) root.add(new BeRealFeed(currentUserId,true),BorderLayout.CENTER);
        else root.add(BeRealTheme.label("Sign in from Main to see your moments.",16,false),BorderLayout.CENTER);
        JPanel controls=BeRealTheme.panel(new GridLayout(2,2,10,10));
        JButton create=BeRealTheme.button("Take a BeReal",true), view=BeRealTheme.button("Refresh moments",false),
            update=BeRealTheme.button("Edit caption",false), delete=BeRealTheme.button("Delete a BeReal",false);
        controls.add(create);controls.add(view);controls.add(update);controls.add(delete);
        create.addActionListener(e->createPost()); view.addActionListener(e->refreshFeed(root));
        update.addActionListener(e->{updatePost();refreshFeed(root);});
        delete.addActionListener(e->{deletePost();refreshFeed(root);});
        root.add(controls,BorderLayout.SOUTH);setContentPane(root);
    }
    private void refreshFeed(JPanel root) {
        for(Component c:root.getComponents()) if(c instanceof BeRealFeed) ((BeRealFeed)c).refresh();
    }

    private JFileChooser chooser() { JFileChooser c=new JFileChooser(); c.setFileFilter(new FileNameExtensionFilter("Image files", "jpg","jpeg","png","gif","bmp")); return c; }
    private void createPost() {
        if(currentUserId<=0) { message("Log in first.");return; }
        JTextField front=new JTextField(),back=new JTextField(),caption=new JTextField();
        BeRealTheme.DualPhoto preview=new BeRealTheme.DualPhoto(null,null,"Choose your two photos");
        preview.setPreferredSize(new Dimension(350,350));
        JButton frontPick=BeRealTheme.button("Front photo",false),backPick=BeRealTheme.button("Back photo",false);
        frontPick.addActionListener(e->{chooseImage(front);preview.setPhotos(back.getText(),front.getText());});
        backPick.addActionListener(e->{chooseImage(back);preview.setPhotos(back.getText(),front.getText());});
        JPanel form=BeRealTheme.panel(new GridLayout(0,2,10,8));
        form.add(frontPick);form.add(backPick);
        form.add(BeRealTheme.note("Front photo"));form.add(front);
        form.add(BeRealTheme.note("Back photo"));form.add(back);
        form.add(BeRealTheme.note("Caption"));form.add(caption);
        JPanel body=BeRealTheme.panel(new BorderLayout(0,12));body.add(preview,BorderLayout.CENTER);body.add(form,BorderLayout.SOUTH);
        JScrollPane scroll=BeRealTheme.scroll(body);scroll.setPreferredSize(new Dimension(440,590));
        while(true){
            if(JOptionPane.showConfirmDialog(this,scroll,"Take a BeReal",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)!=JOptionPane.OK_OPTION)return;
            try{
                if(front.getText().isBlank() || back.getText().isBlank())throw new IllegalArgumentException("Choose both photos.");
                if(javax.imageio.ImageIO.read(new File(front.getText().trim()))==null || javax.imageio.ImageIO.read(new File(back.getText().trim()))==null)
                    throw new IllegalArgumentException("Choose valid image files.");
                java.time.ZoneId zone=java.time.ZoneId.systemDefault();
                java.time.LocalDateTime now=java.time.LocalDateTime.now(zone);
                java.time.LocalDate today=now.toLocalDate();
                int momentId;double hours;
                try(Connection c=DBConnection.getConnection()){
                    c.setAutoCommit(false);
                    try {
                        // Serializes a given user's post insertions to protect the three/day limit.
                        try(PreparedStatement lock=c.prepareStatement("SELECT UserID FROM `USER` WHERE UserID=? AND COALESCE(AccountStatus,'Active')='Active' FOR UPDATE")){
                            lock.setInt(1,currentUserId);try(ResultSet r=lock.executeQuery()){
                                if(!r.next())throw new SQLException("Your account is not active.");
                            }
                        }
                        try(PreparedStatement count=c.prepareStatement("SELECT COUNT(*) FROM POST WHERE UserID=? AND PostedAt>=? AND PostedAt<?")){
                            count.setInt(1,currentUserId);
                            count.setTimestamp(2,Timestamp.valueOf(today.atStartOfDay()));
                            count.setTimestamp(3,Timestamp.valueOf(today.plusDays(1).atStartOfDay()));
                            try(ResultSet r=count.executeQuery()){r.next();if(r.getInt(1)>=3)throw new SQLException("You have already shared three BeReals today.");}
                        }
                        // Moment is selected by date and timezone, never by user-provided ID.
                        String momentSql="SELECT MomentID, `Date`, `Time` FROM MOMENT WHERE `Date`=? AND TimeZone=? AND `Time`<=? ORDER BY `Time` DESC LIMIT 1";
                        try(PreparedStatement m=c.prepareStatement(momentSql)){
                            m.setDate(1,java.sql.Date.valueOf(today));m.setString(2,zone.getId());m.setTime(3,Time.valueOf(now.toLocalTime()));
                            try(ResultSet r=m.executeQuery()){
                                if(!r.next())throw new SQLException("No BeReal notification has been scheduled for today in " + zone.getId() + ". Ask an admin to create a Moment.");
                                momentId=r.getInt(1);
                                java.time.LocalDateTime scheduled=r.getDate(2).toLocalDate().atTime(r.getTime(3).toLocalTime());
                                hours=Math.max(0,java.time.Duration.between(scheduled,now).toSeconds()/3600.0);
                            }
                        }
                        try(PreparedStatement insert=c.prepareStatement("INSERT INTO POST (FrontCam,BackCam,Caption,RetakeCount,HoursLate,PostedAt,UserID,MomentID) VALUES (?,?,?,0,?,?,?,?)")){
                            insert.setString(1,saveToImages(front.getText().trim()));insert.setString(2,saveToImages(back.getText().trim()));
                            insert.setString(3,caption.getText());insert.setDouble(4,hours);
                            insert.setTimestamp(5,Timestamp.valueOf(now));insert.setInt(6,currentUserId);insert.setInt(7,momentId);
                            insert.executeUpdate();
                        }
                        c.commit();
                    } catch(Exception ex){c.rollback();throw ex;}
                }
                message("BeReal shared.");refreshFeed((JPanel)getContentPane());return;
            }catch(Exception ex){error(ex);}
        }
    }
    // Copies the chosen photo into the images folder and returns a relative path,
    // so posts still show their photos on other computers.
    private String saveToImages(String path) throws java.io.IOException {
        File src=new File(path); File dir=new File("images"); dir.mkdirs();
        File dest=new File(dir, System.currentTimeMillis()+"_"+src.getName());
        java.nio.file.Files.copy(src.toPath(), dest.toPath());
        return "images/"+dest.getName();
    }
    private void chooseImage(JTextField target){JFileChooser c=chooser();if(c.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)target.setText(c.getSelectedFile().getAbsolutePath());}
    private void updatePost(){
        if(currentUserId<=0){message("Log in first.");return;}
        DefaultComboBoxModel<OwnedPost> posts=new DefaultComboBoxModel<>();
        try(Connection c=DBConnection.getConnection();PreparedStatement st=c.prepareStatement("SELECT PostID,Caption,PostedAt FROM POST WHERE UserID=? ORDER BY PostedAt DESC")){
            st.setInt(1,currentUserId);try(ResultSet r=st.executeQuery()){while(r.next())posts.addElement(new OwnedPost(r.getInt(1),r.getString(2),r.getTimestamp(3)));}
            if(posts.getSize()==0){message("No posts to edit.");return;}
            JComboBox<OwnedPost> choice=new JComboBox<>(posts);
            if(JOptionPane.showConfirmDialog(this,choice,"Select your post",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
            OwnedPost post=(OwnedPost)choice.getSelectedItem();if(post==null)return;
            String caption=JOptionPane.showInputDialog(this,"New caption:",post.caption);
            if(caption==null)return;
            try(PreparedStatement update=c.prepareStatement("UPDATE POST SET Caption=? WHERE PostID=? AND UserID=?")){
                update.setString(1,caption);update.setInt(2,post.id);update.setInt(3,currentUserId);
                message(update.executeUpdate()==1?"Caption updated.":"Post not found.");
            }
        }catch(Exception ex){error(ex);}
    }
    /** Display only the signed-in user's posts, then delete the selected post. */
    private void deletePost() {
        if (currentUserId <= 0) {
            message("Log in before deleting a BeReal.");
            return;
        }

        DefaultComboBoxModel<OwnedPost> model = new DefaultComboBoxModel<>();
        String listSql = "SELECT PostID, Caption, PostedAt FROM POST WHERE UserID=? "
                + "ORDER BY PostedAt DESC, PostID DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(listSql)) {
            stmt.setInt(1, currentUserId);
            try (ResultSet results = stmt.executeQuery()) {
                while (results.next()) {
                    model.addElement(new OwnedPost(results.getInt("PostID"),
                            results.getString("Caption"), results.getTimestamp("PostedAt")));
                }
            }
        } catch (SQLException ex) {
            error(ex);
            return;
        }

        if (model.getSize() == 0) {
            message("You don't have any BeReals to delete.");
            return;
        }

        JComboBox<OwnedPost> selector = new JComboBox<>(model);
        selector.setPreferredSize(new Dimension(420, 36));
        JPanel selection = BeRealTheme.panel(new BorderLayout(0, 10));
        selection.add(BeRealTheme.label("Choose one of your BeReals:", 14, false), BorderLayout.NORTH);
        selection.add(selector, BorderLayout.CENTER);
        if (JOptionPane.showConfirmDialog(this, selection, "Delete a BeReal",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        OwnedPost post = (OwnedPost) selector.getSelectedItem();
        if (post == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Permanently delete this BeReal?\n" + post,
                "Confirm deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        String deleteSql = "DELETE FROM POST WHERE PostID=? AND UserID=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setInt(1, post.id);
            stmt.setInt(2, currentUserId);
            message(stmt.executeUpdate() == 1 ? "BeReal deleted." : "Post not found or no longer yours.");
        } catch (SQLException ex) {
            error(ex);
        }
    }

    private static final class OwnedPost {
        final int id;
        final String caption;
        final Timestamp postedAt;

        OwnedPost(int id, String caption, Timestamp postedAt) {
            this.id = id;
            this.caption = caption;
            this.postedAt = postedAt;
        }

        @Override public String toString() {
            String text = caption == null || caption.isBlank() ? "(No caption)" : caption.trim();
            if (text.length() > 55) text = text.substring(0, 52) + "...";
            return (postedAt == null ? "Unknown date" : postedAt.toString()) + " — " + text;
        }
    }

    private void message(String s){JOptionPane.showMessageDialog(this,s);}private void error(Exception e){JOptionPane.showMessageDialog(this,"Error: "+e.getMessage(),"BeReal",JOptionPane.ERROR_MESSAGE);}public static void main(String[]a){JOptionPane.showMessageDialog(null,"Log in through Main to manage posts.");}
}
