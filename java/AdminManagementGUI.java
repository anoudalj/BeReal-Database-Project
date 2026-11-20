import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/** Administrative moderation based on CSC380 Phase 2 permissions. */
public final class AdminManagementGUI extends JFrame {
    private final int adminId;
    private final DefaultTableModel users = model("Username", "Email", "Role", "Account type", "Status");
    private final DefaultTableModel posts = model("Posted at", "Caption", "Retakes", "Hours late");
    private final DefaultTableModel comments = model("Post date", "Author", "Comment", "Time posted");
    private final JTable userTable = new JTable(users), postTable = new JTable(posts), commentTable = new JTable(comments);
    private final java.util.List<Integer> userIds = new java.util.ArrayList<>();
    private final java.util.List<Integer> postIds = new java.util.ArrayList<>();
    private final java.util.List<int[]> commentKeys = new java.util.ArrayList<>();
    private final JLabel selected = BeRealTheme.note("Select an account to inspect its content.");
    private final JTextField search = new JTextField(20);
    private final JTabbedPane tabs = new JTabbedPane();

    private static DefaultTableModel model(String... headers) {
        return new DefaultTableModel(headers, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }
    public AdminManagementGUI(int adminId) {
        BeRealTheme.install(); this.adminId = adminId;
        setTitle("BeReal. | Admin management"); setSize(1100,760);
        setMinimumSize(new Dimension(850,600)); setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        if (!isAdmin()) {
            JOptionPane.showMessageDialog(null,"Only an authenticated administrator may open this screen.");
            dispose(); return;
        }
        JPanel root=BeRealTheme.panel(new BorderLayout(10,12));
        root.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        root.add(BeRealTheme.header("Administration: select users and moderate their content"),BorderLayout.NORTH);
        JPanel selector=BeRealTheme.panel(new BorderLayout(8,8));
        JPanel searchRow=BeRealTheme.panel(new FlowLayout(FlowLayout.LEFT));
        searchRow.add(BeRealTheme.note("Find username:")); BeRealTheme.input(search); searchRow.add(search);
        JButton find=BeRealTheme.button("Search / refresh users",false);
        find.addActionListener(e->loadUsers());search.addActionListener(e->loadUsers());searchRow.add(find);
        selector.add(searchRow,BorderLayout.NORTH);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane userScroll=BeRealTheme.scroll(userTable);userScroll.setPreferredSize(new Dimension(800,150));
        selector.add(userScroll,BorderLayout.CENTER);
        selector.add(selected,BorderLayout.SOUTH);
        root.add(selector,BorderLayout.CENTER);
        tabs.addTab("Account",accountActions());
        tabs.addTab("Posts",postActions());
        tabs.addTab("Comments",commentActions());
        tabs.addTab("Other operations",otherActions());
        tabs.addTab("Report queries",reportQueries());
        tabs.setPreferredSize(new Dimension(850,330));
        root.add(tabs,BorderLayout.SOUTH);
        userTable.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting()) loadSelected();});
        BeRealTheme.apply(root);setContentPane(root);
        loadUsers();
    }
    private JPanel actionPanel(JTable table, JButton... buttons) {
        JPanel panel=BeRealTheme.panel(new BorderLayout(8,8));
        if(table!=null) {
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            panel.add(BeRealTheme.scroll(table),BorderLayout.CENTER);
        }
        JPanel actions=BeRealTheme.panel(new FlowLayout(FlowLayout.LEFT,8,4));
        for(JButton button:buttons) actions.add(button);
        panel.add(actions,BorderLayout.SOUTH);return panel;
    }
    private JButton button(String text, Runnable action) {
        JButton b=BeRealTheme.button(text,false);b.addActionListener(e->action.run());return b;
    }
    private JPanel accountActions() {
        JPanel p=BeRealTheme.panel(new BorderLayout());
        JTextArea note=new JTextArea("Select an account above. Modify permitted attributes, or suspend a user. Database IDs are never editable.");
        note.setEditable(false);note.setLineWrap(true);note.setWrapStyleWord(true);
        note.setForeground(Color.WHITE);note.setBackground(BeRealTheme.BG);
        p.add(note,BorderLayout.CENTER);
        JPanel buttons=BeRealTheme.panel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(button("Change username",()->changeUser("Username", "Username")));
        buttons.add(button("Change email",()->changeUser("Email", "Email")));
        buttons.add(button("Account privacy",()->chooseUser("Account_Type",new String[]{"Public","Private"})));
        buttons.add(button("Account status",()->chooseUser("AccountStatus",new String[]{"Active","UnderReview","Suspended"})));
        p.add(buttons,BorderLayout.SOUTH);return p;
    }
    private JPanel postActions() {
        return actionPanel(postTable,
            button("Delete selected post",()->deletePost()),
            button("Refresh posts",()->loadSelected()));
    }
    private JPanel commentActions() {
        return actionPanel(commentTable,
            button("Remove selected comment",()->deleteComment()),
            button("Refresh comments",()->loadSelected()));
    }
    private JPanel otherActions() {
        JPanel p=BeRealTheme.panel(new BorderLayout());
        JTextArea info=new JTextArea("Additional operations from the report. These use database identifiers internally and ask for explicit confirmation.\n"
            +"Moment scheduling: add/update a notification.\n"
            +"Moderation: remove a flagged RealMoji, remove a friendship, or restore a mistakenly removed tag.");
        info.setEditable(false);info.setLineWrap(true);info.setWrapStyleWord(true);
        info.setBackground(BeRealTheme.BG);info.setForeground(Color.WHITE);
        p.add(info,BorderLayout.CENTER);
        JPanel controls=BeRealTheme.panel(new FlowLayout(FlowLayout.LEFT));
        controls.add(button("Create moment",()->createMoment()));
        controls.add(button("Correct moment time",()->updateMoment()));
        controls.add(button("Remove RealMoji",()->removeRealMoji()));
        controls.add(button("Restore tag",()->restoreTag()));
        p.add(controls,BorderLayout.SOUTH);return p;
    }

    private JPanel reportQueries() {
        JPanel panel=BeRealTheme.panel(new BorderLayout(10,10));
        JTextArea help=new JTextArea("Read-only retrievals specified in Phase 2. These show their results in a table. These queries do not modify primary keys or content.");
        help.setEditable(false);help.setLineWrap(true);help.setWrapStyleWord(true);
        help.setBackground(BeRealTheme.BG);help.setForeground(Color.WHITE);
        panel.add(help,BorderLayout.NORTH);
        JPanel buttons=BeRealTheme.panel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(button("Under review",()->showQuery("Accounts under review", "SELECT Username,Email,AccountStatus FROM `USER` WHERE AccountStatus='UnderReview'")));
        buttons.add(button("Users over 35",()->showQuery("Users over 35", "SELECT Username,Email,DOB FROM `USER` WHERE TIMESTAMPDIFF(YEAR,DOB,CURDATE())>35")));
        buttons.add(button("Pending friendships",()->showQuery("Pending friend requests", "SELECT A.Username AS Requester, B.Username AS Receiver,F.Date_Requested FROM FRIENDSHIP F JOIN `USER` A ON A.UserID=F.Requester_ID JOIN `USER` B ON B.UserID=F.Receiver_ID WHERE F.Status='Pending'")));
        buttons.add(button("Public users",()->showQuery("Public profiles", "SELECT Username,Bio,ProfilePicture FROM `USER` WHERE Account_Type='Public'")));
        buttons.add(button("Comments with authors",()->showQuery("Comments with authors", "SELECT U.Username,P.PostedAt,C.Content,C.TimePosted FROM COMMENT C JOIN `USER` U ON U.UserID=C.UserID JOIN POST P ON P.PostID=C.PostID ORDER BY C.TimePosted DESC")));
        buttons.add(button("RealMoji reactions",()->showQuery("Reactions with designers", "SELECT D.Username AS Designer, R.Emoji, X.Username AS Reactor, P.PostedAt FROM REACTS_WITH RW JOIN REALMOJIS R ON R.RealMojiID=RW.RealMojiID JOIN POST P ON P.PostID=RW.PostID JOIN `USER` D ON D.UserID=R.UserID JOIN `USER` X ON X.UserID=RW.UserID")));
        panel.add(buttons,BorderLayout.CENTER);
        return panel;
    }
    private void showQuery(String title,String sql) {
        try{
            if(!isAdmin())throw new SQLException("Admin access denied.");
            try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql);ResultSet r=s.executeQuery()){
                ResultSetMetaData md=r.getMetaData();int count=md.getColumnCount();
                String[] names=new String[count];for(int i=0;i<count;i++)names[i]=md.getColumnLabel(i+1);
                DefaultTableModel table=model(names);
                while(r.next()){
                    Object[] values=new Object[count];for(int i=0;i<count;i++)values[i]=r.getObject(i+1);
                    table.addRow(values);
                }
                JTable result=new JTable(table);result.setAutoCreateRowSorter(true);
                JScrollPane scroll=new JScrollPane(result);scroll.setPreferredSize(new Dimension(780,380));
                JOptionPane.showMessageDialog(this,scroll,title+" ("+table.getRowCount()+" rows)",JOptionPane.PLAIN_MESSAGE);
            }
        }catch(Exception e){fail(e);}
    }
    private boolean isAdmin() {
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("SELECT UserType FROM `USER` WHERE UserID=?")){
            s.setInt(1,adminId);try(ResultSet r=s.executeQuery()){return r.next() && "Admin".equalsIgnoreCase(r.getString(1));}
        } catch(SQLException e){return false;}
    }
    private void fail(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Database error",JOptionPane.ERROR_MESSAGE);}
    private int chosenUser() {
        int row=userTable.getSelectedRow();
        if(row<0) throw new IllegalStateException("Select a user from the user list first.");
        return userIds.get(userTable.convertRowIndexToModel(row));
    }
    private int selectedIndex(JTable table,int size) {
        int row=table.getSelectedRow();
        if(row<0) throw new IllegalStateException("Select a record from the list first.");
        int index=table.convertRowIndexToModel(row);
        if(index>=size) throw new IllegalStateException("Refresh and select the record again.");
        return index;
    }
    private boolean confirm(String question) { return JOptionPane.showConfirmDialog(this,question,"Confirm admin action",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)==JOptionPane.YES_OPTION; }
    private void loadUsers() {
        String sql="SELECT UserID, Username, Email, UserType, Account_Type, AccountStatus FROM `USER` WHERE Username LIKE ? ORDER BY Username";
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){
            s.setString(1,"%"+search.getText().trim()+"%");
            users.setRowCount(0);userIds.clear();posts.setRowCount(0);postIds.clear();comments.setRowCount(0);commentKeys.clear();
            try(ResultSet r=s.executeQuery()){while(r.next()){
                userIds.add(r.getInt("UserID"));users.addRow(new Object[]{r.getString("Username"),r.getString("Email"),r.getString("UserType"),r.getString("Account_Type"),r.getString("AccountStatus")});
            }} selected.setText("Select an account to inspect its content.");
        } catch(SQLException e){fail(e);}
    }
    private void loadSelected() {
        if(userTable.getSelectedRow()<0) return;
        try {
            int uid=chosenUser();String username=String.valueOf(userTable.getValueAt(userTable.getSelectedRow(),0));
            selected.setText("Managing @"+username+" — posts and comments shown below");
            posts.setRowCount(0);postIds.clear();comments.setRowCount(0);commentKeys.clear();
            try(Connection c=DBConnection.getConnection()){
                try(PreparedStatement s=c.prepareStatement("SELECT PostID,PostedAt,Caption,RetakeCount,HoursLate FROM POST WHERE UserID=? ORDER BY PostedAt DESC,PostID DESC")){
                    s.setInt(1,uid);try(ResultSet r=s.executeQuery()){while(r.next()){
                        postIds.add(r.getInt(1));posts.addRow(new Object[]{r.getTimestamp(2),r.getString(3),r.getInt(4),r.getDouble(5)});
                    }}
                }
                // A user's posts and comments made by that user are both available to moderators.
                try(PreparedStatement s=c.prepareStatement("SELECT C.PostID,C.CommentNumber,P.PostedAt,U.Username,C.Content,C.TimePosted "
                    +"FROM COMMENT C JOIN POST P ON P.PostID=C.PostID JOIN `USER` U ON U.UserID=C.UserID "
                    +"WHERE P.UserID=? OR C.UserID=? ORDER BY C.TimePosted DESC")){
                    s.setInt(1,uid);s.setInt(2,uid);try(ResultSet r=s.executeQuery()){while(r.next()){
                        commentKeys.add(new int[]{r.getInt(1),r.getInt(2)});
                        comments.addRow(new Object[]{r.getTimestamp(3),r.getString(4),r.getString(5),r.getTimestamp(6)});
                    }}
                }
            }
        } catch(Exception e){fail(e);}
    }
    private void changeUser(String column,String label) {
        try {
            int uid=chosenUser();String value=JOptionPane.showInputDialog(this,"New "+label+":");
            if(value==null)return;value=value.trim();
            if(value.isBlank() || value.length()>255)throw new IllegalArgumentException("Value must not be empty or too long.");
            if("Email".equals(column) && !value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new IllegalArgumentException("Enter a valid email.");
            if(!confirm("Change "+label+" for selected user?"))return;
            update("UPDATE `USER` SET "+column+"=? WHERE UserID=?",value,uid);
            loadUsers();
        }catch(Exception e){fail(e);}
    }
    private void chooseUser(String column,String[] choices) {
        try{
            int uid=chosenUser();String choice=(String)JOptionPane.showInputDialog(this,"Choose "+column,"Account settings",JOptionPane.QUESTION_MESSAGE,null,choices,choices[0]);
            if(choice==null || !confirm("Set "+column+" to "+choice+"?"))return;
            update("UPDATE `USER` SET "+column+"=? WHERE UserID=?",choice,uid);loadUsers();
        }catch(Exception e){fail(e);}
    }
    private void update(String sql,String value,int id) throws SQLException {
        if(!isAdmin())throw new SQLException("Admin access has been revoked.");
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){
            s.setString(1,value);s.setInt(2,id);if(s.executeUpdate()!=1)throw new SQLException("Record was not updated.");
        }
    }
    private void deletePost() {
        try{
            if(!isAdmin())throw new SQLException("Admin access denied.");
            int idx=selectedIndex(postTable,postIds.size()),id=postIds.get(idx);
            String caption=String.valueOf(posts.getValueAt(idx,1));
            if(!confirm("Permanently remove this post and its comments, reactions and tags?\n"+caption))return;
            try(Connection c=DBConnection.getConnection()){
                c.setAutoCommit(false);
                try{
                    for(String table:new String[]{"REACTS_WITH","TAGGED_ON","COMMENT"}) {
                        try(PreparedStatement s=c.prepareStatement("DELETE FROM "+table+" WHERE PostID=?")){s.setInt(1,id);s.executeUpdate();}
                    }
                    try(PreparedStatement s=c.prepareStatement("DELETE FROM POST WHERE PostID=?")){s.setInt(1,id);if(s.executeUpdate()!=1)throw new SQLException("Post no longer exists.");}
                    c.commit();
                }catch(SQLException e){c.rollback();throw e;}
            }
            loadSelected();JOptionPane.showMessageDialog(this,"Post removed.");
        }catch(Exception e){fail(e);}
    }
    private void deleteComment() {
        try{
            if(!isAdmin())throw new SQLException("Admin access denied.");
            int idx=selectedIndex(commentTable,commentKeys.size());int[] key=commentKeys.get(idx);
            if(!confirm("Permanently remove this comment?\n"+comments.getValueAt(idx,2)))return;
            try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("DELETE FROM COMMENT WHERE PostID=? AND CommentNumber=?")){
                s.setInt(1,key[0]);s.setInt(2,key[1]);if(s.executeUpdate()!=1)throw new SQLException("Comment no longer exists.");
            }loadSelected();
        }catch(Exception e){fail(e);}
    }
    private String ask(String prompt){return JOptionPane.showInputDialog(this,prompt);}
    private void createMoment(){
        try {
            if(!isAdmin())throw new SQLException("Admin access denied.");
            String d=ask("Moment date (YYYY-MM-DD):");if(d==null)return;
            String t=ask("Moment time (HH:MM:SS):");if(t==null)return;
            String z=ask("Time zone:");if(z==null)return;
            java.sql.Date date=java.sql.Date.valueOf(d.trim());java.sql.Time time=java.sql.Time.valueOf(t.trim());
            if(z.trim().isEmpty())throw new IllegalArgumentException("Time zone is required.");
            try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("INSERT INTO MOMENT (`Date`,`Time`,TimeZone) VALUES (?,?,?)")){
                s.setDate(1,date);s.setTime(2,time);s.setString(3,z.trim());s.executeUpdate();
            }JOptionPane.showMessageDialog(this,"Moment added.");
        }catch(Exception e){fail(e);}
    }
    private void updateMoment(){
        try{
            if(!isAdmin())throw new SQLException("Admin access denied.");
            String d=ask("Moment date (YYYY-MM-DD) to find:");if(d==null)return;
            String z=ask("Moment time zone to find:");if(z==null)return;
            String t=ask("Corrected time (HH:MM:SS):");if(t==null)return;
            try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("UPDATE MOMENT SET `Time`=? WHERE `Date`=? AND TimeZone=?")){
                s.setTime(1,java.sql.Time.valueOf(t.trim()));s.setDate(2,java.sql.Date.valueOf(d.trim()));s.setString(3,z.trim());
                int count=s.executeUpdate();JOptionPane.showMessageDialog(this,count+" moment(s) updated.");
            }
        }catch(Exception e){fail(e);}
    }
    private void removeRealMoji(){
        try{
            if(!isAdmin())throw new SQLException("Admin access denied.");
            int user=chosenUser();String id=ask("Flagged RealMoji ID belonging to selected user:");if(id==null)return;
            if(!confirm("Delete this RealMoji and its reactions?"))return;
            try(Connection c=DBConnection.getConnection()){
                c.setAutoCommit(false);
                try{
                    try(PreparedStatement s=c.prepareStatement("DELETE FROM REACTS_WITH WHERE RealMojiID=?")){s.setInt(1,Integer.parseInt(id));s.executeUpdate();}
                    try(PreparedStatement s=c.prepareStatement("DELETE FROM REALMOJIS WHERE RealMojiID=? AND UserID=?")){
                        s.setInt(1,Integer.parseInt(id));s.setInt(2,user);if(s.executeUpdate()!=1)throw new SQLException("RealMoji not found for selected user.");
                    }c.commit();
                }catch(SQLException e){c.rollback();throw e;}
            }JOptionPane.showMessageDialog(this,"RealMoji removed.");
        }catch(Exception e){fail(e);}
    }
    private void restoreTag(){
        try{
            if(!isAdmin())throw new SQLException("Admin access denied.");
            int user=chosenUser();String pid=ask("Post ID to restore tag on (must exist):");if(pid==null)return;
            if(!confirm("Tag selected user in this post?"))return;
            try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("INSERT INTO TAGGED_ON (UserID,PostID) VALUES (?,?)")){
                s.setInt(1,user);s.setInt(2,Integer.parseInt(pid.trim()));s.executeUpdate();
            }JOptionPane.showMessageDialog(this,"Tag restored.");
        }catch(Exception e){fail(e);}
    }
}
