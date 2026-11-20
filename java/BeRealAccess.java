import java.sql.*;

/** Shared data-access policies. Internal IDs never need to be entered by a user. */
public final class BeRealAccess {
    private BeRealAccess() { }
    public static boolean active(Connection c, int uid) throws SQLException {
        try (PreparedStatement s=c.prepareStatement("SELECT COUNT(*) FROM `USER` WHERE UserID=? AND COALESCE(AccountStatus,'Active')='Active'")) {
            s.setInt(1,uid); try(ResultSet r=s.executeQuery()){r.next();return r.getInt(1)>0;}
        }
    }
    public static boolean friends(Connection c,int a,int b) throws SQLException {
        try(PreparedStatement s=c.prepareStatement("SELECT COUNT(*) FROM FRIENDSHIP WHERE Status='Accepted' AND ((Requester_ID=? AND Receiver_ID=?) OR (Requester_ID=? AND Receiver_ID=?))")) {
            s.setInt(1,a);s.setInt(2,b);s.setInt(3,b);s.setInt(4,a);
            try(ResultSet r=s.executeQuery()){r.next();return r.getInt(1)>0;}
        }
    }
    public static boolean canView(Connection c,int viewer,int postId) throws SQLException {
        if(!active(c,viewer)) return false;
        String sql="SELECT P.UserID, U.Account_Type, COALESCE(U.AccountStatus,'Active') FROM POST P JOIN `USER` U ON U.UserID=P.UserID WHERE P.PostID=?";
        try(PreparedStatement s=c.prepareStatement(sql)){
            s.setInt(1,postId);try(ResultSet r=s.executeQuery()){
                if(!r.next() || !"Active".equalsIgnoreCase(r.getString(3)))return false;
                int owner=r.getInt(1);
                if(owner==viewer || "Public".equalsIgnoreCase(r.getString(2)))return true;
                if(!friends(c,viewer,owner))return false;
                return hasPosted(c,viewer);
            }
        }
    }
    public static boolean canInteract(Connection c,int viewer,int postId) throws SQLException {
        if(!active(c,viewer))return false;
        try(PreparedStatement s=c.prepareStatement("SELECT P.UserID FROM POST P JOIN `USER` U ON P.UserID=U.UserID WHERE P.PostID=? AND COALESCE(U.AccountStatus,'Active')='Active'")){
            s.setInt(1,postId);try(ResultSet r=s.executeQuery()){
                if(!r.next())return false;
                int owner=r.getInt(1);
                return owner!=viewer && friends(c,viewer,owner) && hasPosted(c,viewer);
            }
        }
    }
    public static boolean hasPosted(Connection c,int uid) throws SQLException {
        try(PreparedStatement s=c.prepareStatement("SELECT COUNT(*) FROM POST WHERE UserID=?")){
            s.setInt(1,uid);try(ResultSet r=s.executeQuery()){r.next();return r.getInt(1)>0;}
        }
    }
    public static int userId(Connection c,String username) throws SQLException {
        try(PreparedStatement s=c.prepareStatement("SELECT UserID FROM `USER` WHERE Username=? AND COALESCE(AccountStatus,'Active')='Active'")){
            s.setString(1,username);try(ResultSet r=s.executeQuery()){if(!r.next())throw new SQLException("No active account found for @"+username);return r.getInt(1);}
        }
    }
    /** Count uninterrupted scheduled notification moments in the user's latest posting time zone. */
    public static int streak(Connection c,int uid) throws SQLException {
        String zone=null;
        try(PreparedStatement st=c.prepareStatement("SELECT M.TimeZone FROM POST P JOIN MOMENT M ON M.MomentID=P.MomentID WHERE P.UserID=? ORDER BY M.`Date` DESC,M.`Time` DESC LIMIT 1")){
            st.setInt(1,uid);try(ResultSet r=st.executeQuery()){if(r.next())zone=r.getString(1);}
        }
        if(zone==null)return 0;
        java.time.LocalDateTime now=java.time.LocalDateTime.now();
        java.util.Set<Integer> posted=new java.util.HashSet<>();
        try(PreparedStatement st=c.prepareStatement("SELECT DISTINCT MomentID FROM POST WHERE UserID=?")){
            st.setInt(1,uid);try(ResultSet r=st.executeQuery()){while(r.next())posted.add(r.getInt(1));}
        }
        int count=0;
        try(PreparedStatement st=c.prepareStatement("SELECT MomentID FROM MOMENT WHERE TimeZone=? AND TIMESTAMP(`Date`,`Time`)<=? ORDER BY `Date` DESC,`Time` DESC,MomentID DESC")){
            st.setString(1,zone);st.setTimestamp(2,Timestamp.valueOf(now));
            try(ResultSet r=st.executeQuery()){
                while(r.next()){
                    if(!posted.contains(r.getInt(1)))break;
                    count++;
                }
            }
        }
        return count;
    }
}
