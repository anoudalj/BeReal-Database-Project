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
        this.currentUserId = currentUserId; setTitle("BeReal Posts"); setSize(520,260); setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); setLocationRelativeTo(null);
        JButton create=new JButton("Create Post"), view=new JButton("View My Posts"), update=new JButton("Update Caption"), delete=new JButton("Delete Post");
        JPanel p=new JPanel(new GridLayout(2,2,10,10));p.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));p.add(create);p.add(view);p.add(update);p.add(delete);add(p);
        create.addActionListener(e->createPost());view.addActionListener(e->viewPosts());update.addActionListener(e->updatePost());delete.addActionListener(e->deletePost());
    }
    private JFileChooser chooser() { JFileChooser c=new JFileChooser(); c.setFileFilter(new FileNameExtensionFilter("Image files", "jpg","jpeg","png","gif","bmp")); return c; }
    private void createPost() {
        JTextField front=new JTextField(),back=new JTextField(),caption=new JTextField(),retakes=new JTextField("0"),late=new JTextField("0"),posted=new JTextField("YYYY-MM-DD HH:MM:SS"),moment=new JTextField();
        JButton frontPick=new JButton("Choose front image…"),backPick=new JButton("Choose back image…");
        frontPick.addActionListener(e->chooseImage(front)); backPick.addActionListener(e->chooseImage(back));
        Object[] form={"Front photo",front,frontPick,"Back photo",back,backPick,"Caption",caption,"Retake count",retakes,"Hours late",late,"Posted at",posted,"Moment ID",moment};
        if(JOptionPane.showConfirmDialog(this,form,"Create Post",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        if(front.getText().trim().isEmpty()||back.getText().trim().isEmpty()||moment.getText().trim().isEmpty()){message("Front image, back image, and Moment ID are required.");return;}
        int userId=currentUserId; if(userId<=0){String value=JOptionPane.showInputDialog(this,"User ID:");if(value==null)return;try{userId=Integer.parseInt(value.trim());}catch(NumberFormatException ex){message("Enter a valid User ID.");return;}}
        String sql="INSERT INTO POST (FrontCam, BackCam, Caption, RetakeCount, HoursLate, PostedAt, UserID, MomentID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setString(1,front.getText().trim());s.setString(2,back.getText().trim());s.setString(3,caption.getText());s.setInt(4,Integer.parseInt(retakes.getText().trim()));s.setDouble(5,Double.parseDouble(late.getText().trim()));s.setString(6,posted.getText().trim());s.setInt(7,userId);s.setInt(8,Integer.parseInt(moment.getText().trim()));s.executeUpdate();message("Post created successfully.");}catch(Exception ex){error(ex);}
    }
    private void chooseImage(JTextField target){JFileChooser c=chooser();if(c.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)target.setText(c.getSelectedFile().getAbsolutePath());}
    private void viewPosts(){String sql=currentUserId>0?"SELECT * FROM POST WHERE UserID = ? ORDER BY PostedAt DESC":"SELECT * FROM POST ORDER BY PostedAt DESC";StringBuilder text=new StringBuilder();try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){if(currentUserId>0)s.setInt(1,currentUserId);try(ResultSet r=s.executeQuery()){while(r.next())text.append("Post ").append(r.getInt("PostID")).append("\nFront: ").append(r.getString("FrontCam")).append("\nBack: ").append(r.getString("BackCam")).append("\nCaption: ").append(r.getString("Caption")).append("\n\n");}JTextArea area=new JTextArea(text.length()==0?"No posts found.":text.toString(),16,42);area.setEditable(false);JOptionPane.showMessageDialog(this,new JScrollPane(area),"Posts",JOptionPane.INFORMATION_MESSAGE);}catch(SQLException ex){error(ex);}}
    private void updatePost(){String id=JOptionPane.showInputDialog(this,"Post ID:");if(id==null)return;String caption=JOptionPane.showInputDialog(this,"New caption:");if(caption==null)return;String sql="UPDATE POST SET Caption=? WHERE PostID=?"+(currentUserId>0?" AND UserID=?":"");try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setString(1,caption);s.setInt(2,Integer.parseInt(id.trim()));if(currentUserId>0)s.setInt(3,currentUserId);message(s.executeUpdate()==1?"Post updated.":"Post not found or not yours.");}catch(Exception ex){error(ex);}}
    private void deletePost(){String id=JOptionPane.showInputDialog(this,"Post ID to delete:");if(id==null)return;String sql="DELETE FROM POST WHERE PostID=?"+(currentUserId>0?" AND UserID=?":"");try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setInt(1,Integer.parseInt(id.trim()));if(currentUserId>0)s.setInt(2,currentUserId);message(s.executeUpdate()==1?"Post deleted.":"Post not found or not yours.");}catch(Exception ex){error(ex);}}
    private void message(String s){JOptionPane.showMessageDialog(this,s);}private void error(Exception e){JOptionPane.showMessageDialog(this,"Error: "+e.getMessage(),"BeReal",JOptionPane.ERROR_MESSAGE);}public static void main(String[]a){SwingUtilities.invokeLater(()->new PostGUI().setVisible(true));}
}
