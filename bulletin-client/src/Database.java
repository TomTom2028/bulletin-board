import javax.crypto.spec.SecretKeySpec;
import java.rmi.RemoteException;
import java.security.Key;
import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * bulletin-board: Database
 *
 * @author robbe
 * @version 16/11/2024
 */

public class Database {
    Connection connection;
    public Database() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:client.db");
        //language=SQL
        runStatement("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT," +
                "n INTEGER," +
                "tagSize INTEGER," +
                "sendKey BLOB," +
                "recieveKey BLOB," +
                "sendIdx INTEGER," +
                "sendTag BLOB," +
                "recieveIdx INTEGER," +
                "recieveTag BLOB, " +
                "pending BOOLEAN," +
                "added_at TIMESTAMP NOT NULL);");

    }


    private void runStatement(String statement) throws SQLException {
        connection.createStatement().execute(statement);
    }

    public void addRecieverUser(OtherUser user) throws SQLException {
        //use prepared statement
        //language=SQL
        String sql = "INSERT INTO users (username, n, tagSize, recieveKey, recieveIdx, recieveTag, pending, added_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, user.getUsername());
        statement.setInt(2, user.getRecieverApp().getN());
        statement.setInt(3, user.getRecieverApp().getTagSize());
        statement.setBytes(4, user.getRecieverApp().getSharedKey().getEncoded());
        statement.setInt(5, user.getRecieverApp().getIdx());
        statement.setBytes(6, user.getRecieverApp().getTag());
        statement.setBoolean(7, user.isPending());
        statement.setTimestamp(8, Timestamp.valueOf(user.getAddedAt()));


        statement.executeUpdate();
        user.setId(statement.getGeneratedKeys().getInt(1));

    }


    public List<OtherUser> getOtherUsers() throws SQLException {
        //language=SQL
        String sql = "SELECT id, username, pending, added_at FROM users";
        ResultSet rs = connection.createStatement().executeQuery(sql);
        List<OtherUser> users = new ArrayList<>();
        while (rs.next()) {
            int id = rs.getInt("id");
            String username = rs.getString("username");
            boolean pending = rs.getBoolean("pending");
            LocalDateTime addedAt = rs.getTimestamp("added_at").toLocalDateTime();
            users.add(new OtherUser(null, null, username, new ArrayList<>(), pending, id, this, false, addedAt));
        }
        return users;
    }

    public void updateUsername(OtherUser user, String username) throws SQLException {
        int userId = user.getId();

        //language=SQL
        String sql = "UPDATE users SET username = ? WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, username);
        statement.setInt(2, userId);
        statement.executeUpdate();
    }

    public void populateUser(OtherUser user, BulletinBoard board) throws SQLException, RemoteException {
        int userId = user.getId();
        //language=SQL
        String sql = "SELECT n, tagSize, sendKey, recieveKey, sendIdx, sendTag, recieveIdx, recieveTag FROM users WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, userId);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            byte[] sendKeyBytes = rs.getBytes("sendKey");
            byte[] recieveKeyBytes = rs.getBytes("recieveKey");
            int sendIdx = rs.getInt("sendIdx");
            byte[] sendTag = rs.getBytes("sendTag");
            int recieveIdx = rs.getInt("recieveIdx");
            byte[] recieveTag = rs.getBytes("recieveTag");

            byte[] seed = new SecureRandom().generateSeed(32);
            Key sendKey = null;
            Key recieveKey = null;
            if (sendKeyBytes != null) {
                sendKey = new SecretKeySpec(sendKeyBytes, 0, sendKeyBytes.length, "AES");
            }
            if (recieveKeyBytes != null) {
                recieveKey = new SecretKeySpec(recieveKeyBytes, 0, recieveKeyBytes.length, "AES");
            }
            user.setSenderApp(new ClientApplication(seed, sendKey, sendIdx, sendTag, board));
            user.setRecieverApp(new ClientApplication(seed, recieveKey, recieveIdx, recieveTag, board));
        }
    }

}