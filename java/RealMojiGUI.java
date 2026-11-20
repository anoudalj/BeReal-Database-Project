import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.nio.file.*;

/** RealMoji creator/manager for the signed-in user. IDs are internal. */
public final class RealMojiGUI extends JFrame {
    private final int userId;
    private final DefaultTableModel model=new DefaultTableModel(new String[]{"Emoji", "Image"},0){
        @Override public boolean isCellEditable(int row,int column){return false;}
    };
    private final java.util.List<Integer> keys=new java.util.ArrayList<>();
    private final JTable table=new JTable(model);
    public RealMojiGUI(int userId){
        this.userId=userId;BeRealTheme.install();
        setTitle("BeReal. | My RealMojis");setSize(620,470);setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel root=BeRealTheme.panel(new BorderLayout(12,12));root.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        root.add(BeRealTheme.header("Design and manage your reactions"),BorderLayout.NORTH);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        root.add(BeRealTheme.scroll(table),BorderLayout.CENTER);
        JPanel bottom=BeRealTheme.panel(new FlowLayout());
        JButton add=BeRealTheme.button("Create RealMoji",true),remove=BeRealTheme.button("Delete selected",false);
        add.addActionListener(e->create());remove.addActionListener(e->remove());
        bottom.add(add);bottom.add(remove);root.add(bottom,BorderLayout.SOUTH);
        setContentPane(root);refresh();
    }
    private void refresh(){
        model.setRowCount(0);keys.clear();
        try(Connection c=DBConnection.getConnection();PreparedStatement st=c.prepareStatement("SELECT RealMojiID,Emoji,Image FROM REALMOJIS WHERE UserID=? ORDER BY RealMojiID DESC")){
            st.setInt(1,userId);try(ResultSet r=st.executeQuery()){while(r.next()){
                keys.add(r.getInt(1));model.addRow(new Object[]{r.getString(2),r.getString(3)});
            }}
        }catch(Exception ex){error(ex);}
    }
    private void create(){
        String emoji=JOptionPane.showInputDialog(this,"Reaction name / emoji:");
        if(emoji==null)return;
        emoji=emoji.trim();if(emoji.isEmpty()||emoji.length()>50){error(new Exception("Enter a name with at most 50 characters."));return;}
        JFileChooser chooser=new JFileChooser();chooser.setFileFilter(new FileNameExtensionFilter("Images","jpg","jpeg","png","gif"));
        if(chooser.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        File file=chooser.getSelectedFile();
        try{
            if(javax.imageio.ImageIO.read(file)==null)throw new Exception("Choose a valid image.");
            try(Connection c=DBConnection.getConnection()){
                if(!BeRealAccess.active(c,userId))throw new Exception("Account is not active.");
                Path folder=Paths.get("images");Files.createDirectories(folder);
                Path target=folder.resolve("realmoji_"+java.util.UUID.randomUUID()+"_"+file.getName());
                Files.copy(file.toPath(),target);
                try(PreparedStatement st=c.prepareStatement("INSERT INTO REALMOJIS (Emoji,Image,UserID) VALUES (?,?,?)")){
                    st.setString(1,emoji);st.setString(2,target.toString());st.setInt(3,userId);st.executeUpdate();
                }catch(Exception ex){Files.deleteIfExists(target);throw ex;}
            }refresh();
        }catch(Exception ex){error(ex);}
    }
    private void remove(){
        int row=table.getSelectedRow();if(row<0){error(new Exception("Select a RealMoji first."));return;}
        int id=keys.get(table.convertRowIndexToModel(row));
        if(JOptionPane.showConfirmDialog(this,"Delete selected RealMoji and its reactions?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try {
                if(!BeRealAccess.active(c,userId))throw new SQLException("Account is not active.");
                try(PreparedStatement st=c.prepareStatement("DELETE FROM REACTS_WITH WHERE RealMojiID=?")){
                    st.setInt(1,id);st.executeUpdate();
                }
                try(PreparedStatement st=c.prepareStatement("DELETE FROM REALMOJIS WHERE RealMojiID=? AND UserID=?")){
                    st.setInt(1,id);st.setInt(2,userId);st.executeUpdate();
                }c.commit();
            }catch(Exception ex){c.rollback();throw ex;}
            refresh();
        }catch(Exception ex){error(ex);}
    }
    private void error(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"RealMoji",JOptionPane.ERROR_MESSAGE);}
}
