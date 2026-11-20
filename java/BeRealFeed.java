import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Photo feed for the signed-in account. All database reads run off the UI thread. */
public class BeRealFeed extends JPanel {
    private final int userId;
    private final JPanel list = BeRealTheme.panel(new GridBagLayout());
    private final JButton friends = BeRealTheme.button("My Friends", true);
    private final JButton mine = BeRealTheme.button("My BeReal", false);
    private boolean ownOnly;
    private int generation;

    public BeRealFeed(int userId) { this(userId, false); }
    public BeRealFeed(int userId, boolean ownOnly) {
        super(new BorderLayout(0, 20)); this.userId = userId; this.ownOnly = ownOnly; setBackground(Color.BLACK);
        JPanel tabs = BeRealTheme.panel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        tabs.add(friends); tabs.add(mine);
        JButton refresh = BeRealTheme.button("Refresh", false); tabs.add(refresh);
        friends.addActionListener(e -> { this.ownOnly = false; refresh(); });
        mine.addActionListener(e -> { this.ownOnly = true; refresh(); });
        refresh.addActionListener(e -> refresh());
        add(tabs, BorderLayout.NORTH); add(BeRealTheme.scroll(list), BorderLayout.CENTER);
        refresh();
    }
    public void refresh() {
        final boolean onlyMine = ownOnly; final int request = ++generation;
        friends.setForeground(onlyMine ? BeRealTheme.MUTED : Color.WHITE);
        friends.setBackground(BeRealTheme.SURFACE);
        mine.setForeground(onlyMine ? Color.WHITE : BeRealTheme.MUTED);
        mine.setBackground(BeRealTheme.SURFACE);
        status("Loading your BeReal…");
        new SwingWorker<List<Post>, Void>() {
            protected List<Post> doInBackground() throws Exception {
                String sql="SELECT P.PostID,P.UserID,U.Username,P.BackCam,P.FrontCam,P.Caption,P.PostedAt "
                    +"FROM POST P JOIN `USER` U ON U.UserID=P.UserID WHERE COALESCE(U.AccountStatus,'Active')='Active' AND "
                    +(onlyMine ? "P.UserID=? " : "(P.UserID=? OR U.Account_Type='Public' OR (EXISTS "
                    +"(SELECT 1 FROM FRIENDSHIP F WHERE F.Status='Accepted' AND ((F.Requester_ID=? AND F.Receiver_ID=P.UserID) OR "
                    +"(F.Receiver_ID=? AND F.Requester_ID=P.UserID))) AND EXISTS (SELECT 1 FROM POST X WHERE X.UserID=?))) ")
                    +"ORDER BY P.PostedAt DESC,P.PostID DESC LIMIT 50";
                List<Post> posts = new ArrayList<>();
                try (Connection c=DBConnection.getConnection(); PreparedStatement s=c.prepareStatement(sql)) {
                    if(!BeRealAccess.active(c,userId)) throw new SQLException("Account is inactive. Please contact an administrator.");
                    s.setInt(1,userId); if(!onlyMine) { s.setInt(2,userId); s.setInt(3,userId); s.setInt(4,userId); }
                    try (ResultSet r=s.executeQuery()) {
                        while(r.next()) posts.add(new Post(r.getInt(1),r.getInt(2),r.getString(3),
                            r.getString(4),r.getString(5),r.getString(6),r.getString(7)));
                    }
                }
                return posts;
            }
            protected void done() {
                if(request != generation) return;
                try {
                    List<Post> posts=get(); list.removeAll();
                    if(posts.isEmpty()) { status(onlyMine ? "Your first BeReal is waiting. Take a BeReal to get started." : "No posts yet. Add friends or share your first BeReal."); return; }
                    int row=0;
                    for(Post post:posts) {
                        GridBagConstraints gc=new GridBagConstraints(); gc.gridx=0; gc.gridy=row++;
                        gc.weightx=1; gc.fill=GridBagConstraints.HORIZONTAL; gc.insets=new Insets(0,0,26,0);
                        list.add(card(post),gc);
                    }
                    GridBagConstraints space=new GridBagConstraints(); space.gridy=row; space.weighty=1;
                    list.add(Box.createVerticalGlue(),space); list.revalidate(); list.repaint();
                } catch(Exception ex) { Throwable cause=ex.getCause()==null?ex:ex.getCause(); status("Could not load posts: "+cause.getMessage()); }
            }
        }.execute();
    }
    private void status(String message) {
        list.removeAll(); JTextArea t=new JTextArea(message); t.setEditable(false); t.setLineWrap(true);
        t.setWrapStyleWord(true); t.setForeground(BeRealTheme.MUTED); t.setBackground(Color.BLACK);
        t.setFont(new Font("SansSerif",Font.PLAIN,14)); t.setBorder(BorderFactory.createEmptyBorder(35,20,35,20));
        GridBagConstraints c=new GridBagConstraints(); c.weightx=1;c.fill=GridBagConstraints.HORIZONTAL;list.add(t,c);
        list.revalidate(); list.repaint();
    }
    private JPanel card(Post p) {
        JPanel card=BeRealTheme.panel(new BorderLayout(0,10));
        JPanel heading=BeRealTheme.panel(new BorderLayout(8,0));
        JLabel avatar=BeRealTheme.label(p.username==null?"?":p.username.substring(0,1).toUpperCase(),20,true);
        avatar.setHorizontalAlignment(SwingConstants.CENTER); avatar.setOpaque(true); avatar.setBackground(BeRealTheme.SURFACE);
        avatar.setPreferredSize(new Dimension(42,42)); heading.add(avatar,BorderLayout.WEST);
        JPanel name=BeRealTheme.panel(new GridLayout(2,1)); name.add(BeRealTheme.label(p.username,14,true));
        name.add(BeRealTheme.note(p.posted==null?"":p.posted)); heading.add(name,BorderLayout.CENTER);
        card.add(heading,BorderLayout.NORTH);
        card.add(new BeRealTheme.DualPhoto(p.back,p.front,"Photo unavailable on this computer"),BorderLayout.CENTER);
        JPanel footer=BeRealTheme.panel(new BorderLayout(0,9));
        JTextArea caption=new JTextArea(p.caption==null?"":p.caption); caption.setEditable(false);caption.setLineWrap(true);
        caption.setWrapStyleWord(true);caption.setBackground(Color.BLACK);caption.setForeground(Color.WHITE);
        caption.setFont(new Font("SansSerif",Font.PLAIN,14));footer.add(caption,BorderLayout.NORTH);
        JPanel actions=BeRealTheme.panel(new FlowLayout(FlowLayout.LEFT,0,0));
        JButton comment=BeRealTheme.button("Comments & RealMojis",false);
        comment.addActionListener(e -> new InteractionsGUI(userId,p.id).setVisible(true));actions.add(comment);
        if(p.owner==userId) {
            JButton edit=BeRealTheme.button("Edit",false);edit.addActionListener(e -> new PostGUI(userId).setVisible(true));actions.add(edit);
        }
        footer.add(actions,BorderLayout.SOUTH);card.add(footer,BorderLayout.SOUTH);return card;
    }
    private static class Post {
        final int id,owner; final String username,back,front,caption,posted;
        Post(int id,int owner,String username,String back,String front,String caption,String posted) {
            this.id=id;this.owner=owner;this.username=username;this.back=back;this.front=front;this.caption=caption;this.posted=posted;
        }
    }
}
