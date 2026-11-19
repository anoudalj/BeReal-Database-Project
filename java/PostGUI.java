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
        JTextField front=new JTextField(), back=new JTextField(), caption=new JTextField(),
            retakes=new JTextField("0"), late=new JTextField("0"),
            posted=new JTextField(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))),
            moment=new JTextField(latestMomentId());        BeRealTheme.DualPhoto preview=new BeRealTheme.DualPhoto(null,null,"Choose your two photos");
        preview.setPreferredSize(new Dimension(350,360));
        JButton frontPick=BeRealTheme.button("Front photo",false),backPick=BeRealTheme.button("Back photo",false);
        frontPick.addActionListener(e->{chooseImage(front);preview.setPhotos(back.getText(),front.getText());});
        backPick.addActionListener(e->{chooseImage(back);preview.setPhotos(back.getText(),front.getText());});
        JPanel form=BeRealTheme.panel(new GridLayout(0,2,10,8));
        form.add(frontPick);form.add(backPick);
        String[] labels={"Front photo path","Back photo path","Add a caption…","Retakes","Hours late","Posted at","Moment ID"};
        JTextField[] fields={front,back,caption,retakes,late,posted,moment};
        for(int i=0;i<fields.length;i++) {form.add(BeRealTheme.note(labels[i]));BeRealTheme.input(fields[i]);form.add(fields[i]);}
        JPanel body=BeRealTheme.panel(new BorderLayout(0,16));body.add(preview,BorderLayout.CENTER);body.add(form,BorderLayout.SOUTH);
        JScrollPane scroll=BeRealTheme.scroll(body);scroll.setPreferredSize(new Dimension(440,650));
        while(true) {
            if(JOptionPane.showConfirmDialog(this,scroll,"Take a BeReal",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)!=JOptionPane.OK_OPTION)return;
            try {
                if(front.getText().isBlank()||back.getText().isBlank())throw new IllegalArgumentException("Choose both a front and back photo.");
                if(javax.imageio.ImageIO.read(new File(front.getText().trim()))==null || javax.imageio.ImageIO.read(new File(back.getText().trim()))==null)
                    throw new IllegalArgumentException("Choose valid image files.");
                int n=Integer.parseInt(retakes.getText().trim()),m=Integer.parseInt(moment.getText().trim());
                double hours=Double.parseDouble(late.getText().trim());
                if(n<0||m<=0||!Double.isFinite(hours)||hours<0)throw new IllegalArgumentException("Retakes and hours late must be nonnegative; Moment ID must be positive.");
                Timestamp timestamp=Timestamp.valueOf(posted.getText().trim());
                int owner=currentUserId;
                if(owner<=0) {String id=JOptionPane.showInputDialog(this,"User ID:");if(id==null)return;owner=Integer.parseInt(id.trim());if(owner<=0)throw new IllegalArgumentException("Enter a positive User ID.");}
                String sql="INSERT INTO POST (FrontCam, BackCam, Caption, RetakeCount, HoursLate, PostedAt, UserID, MomentID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try(Connection c=DBConnection.getConnection();PreparedStatement st=c.prepareStatement(sql)) {
                    st.setString(1,front.getText().trim());st.setString(2,back.getText().trim());st.setString(3,caption.getText());
                    st.setInt(4,n);st.setDouble(5,hours);st.setTimestamp(6,timestamp);st.setInt(7,owner);st.setInt(8,m);st.executeUpdate();
                }
                message("BeReal shared.");refreshFeed((JPanel)getContentPane());return;
            } catch(NumberFormatException ex) {message("Enter valid numbers for retakes, hours late, and Moment ID.");}
            catch(Exception ex) {error(ex);}
        }
    }
        private String latestMomentId() {
        String sql="SELECT MomentID FROM MOMENT ORDER BY `Date` DESC, `Time` DESC LIMIT 1";
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql);ResultSet r=s.executeQuery()) {
            return r.next()?String.valueOf(r.getInt(1)):"";
        } catch(Exception ex) { return ""; }
    }
    private void chooseImage(JTextField target){JFileChooser c=chooser();if(c.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)target.setText(c.getSelectedFile().getAbsolutePath());}
    private void updatePost(){String id=JOptionPane.showInputDialog(this,"Post ID:");if(id==null)return;String caption=JOptionPane.showInputDialog(this,"New caption:");if(caption==null)return;String sql="UPDATE POST SET Caption=? WHERE PostID=?"+(currentUserId>0?" AND UserID=?":"");try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setString(1,caption);s.setInt(2,Integer.parseInt(id.trim()));if(currentUserId>0)s.setInt(3,currentUserId);message(s.executeUpdate()==1?"Post updated.":"Post not found or not yours.");}catch(Exception ex){error(ex);}}
    private void deletePost(){String id=JOptionPane.showInputDialog(this,"Post ID to delete:");if(id==null)return;if(JOptionPane.showConfirmDialog(this,"Delete this BeReal?","Delete BeReal",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;String sql="DELETE FROM POST WHERE PostID=?"+(currentUserId>0?" AND UserID=?":"");try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setInt(1,Integer.parseInt(id.trim()));if(currentUserId>0)s.setInt(2,currentUserId);message(s.executeUpdate()==1?"Post deleted.":"Post not found or not yours.");}catch(Exception ex){error(ex);}}
    private void message(String s){JOptionPane.showMessageDialog(this,s);}private void error(Exception e){JOptionPane.showMessageDialog(this,"Error: "+e.getMessage(),"BeReal",JOptionPane.ERROR_MESSAGE);}public static void main(String[]a){SwingUtilities.invokeLater(()->new PostGUI().setVisible(true));}
}
