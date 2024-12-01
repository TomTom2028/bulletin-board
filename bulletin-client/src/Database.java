import javax.crypto.spec.SecretKeySpec;
import java.rmi.RemoteException;
import java.security.Key;
import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    public Database(String filepath) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + filepath);
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
        //language=SQL
        runStatement("""
                create table if not exists messages
                (
                    id            integer   not null
                        constraint id_fk
                            primary key autoincrement,
                    content       TEXT      not null,
                    sendByMe      BOOLEAN   not null,
                    sendAt        TIMESTAMP not null,
                    other_user_id integer   not null,
                    CONSTRAINT other_user_fk
                        FOREIGN KEY (other_user_id)
                            REFERENCES users (id)
                            ON DELETE CASCADE
                            ON UPDATE CASCADE
                );
                """);
    }


    private void runStatement(String statement) throws SQLException {
        connection.createStatement().execute(statement);
    }

    public void addRecieverUser(OtherUser user) throws SQLException {
        //use prepared statement
        //language=SQL
        String sql = "INSERT INTO users (username, n, tagSize, recieveKey, recieveIdx, recieveTag, pending, added_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        ClientApplication app = user.getApplication();
        statement.setString(1, user.getUsername());
        statement.setInt(2, app.getN());
        statement.setInt(3,app.getTagSize());
        statement.setBytes(4, app.getOtherKey().getEncoded());
        statement.setInt(5, app.getOtherIdx());
        statement.setBytes(6, app.getOtherTag());
        statement.setBoolean(7, user.isPending());
        statement.setTimestamp(8, Timestamp.valueOf(user.getAddedAt()));

        statement.executeUpdate();
        user.setId(statement.getGeneratedKeys().getInt(1));
    }

    public void addCompleteUser(OtherUser user) throws SQLException {
        //use prepared statement
        //language=SQL
        String sql = "INSERT INTO users (username, n, tagSize, sendKey, recieveKey, sendIdx, sendTag, recieveIdx, recieveTag, pending, added_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        ClientApplication app = user.getApplication();
        statement.setString(1, user.getUsername());
        statement.setInt(2, app.getN());
        statement.setInt(3,app.getTagSize());
        statement.setBytes(4, app.getSharedKey().getEncoded());
        statement.setBytes(5, app.getOtherKey().getEncoded());
        statement.setInt(6, app.getIdx());
        statement.setBytes(7, app.getTag());
        statement.setInt(8, app.getOtherIdx());
        statement.setBytes(9, app.getOtherTag());
        statement.setBoolean(10, user.isPending());
        statement.setTimestamp(11, Timestamp.valueOf(user.getAddedAt()));

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
            users.add(new OtherUser(null, username, new ArrayList<>(), pending, id, this, false, addedAt));
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


            List<Message> messages = getMessages(user);
            user.setMessages(messages);

            ClientApplication app = new ClientApplication(seed, sendKey, recieveKey, sendIdx, sendTag, recieveIdx, recieveTag, board, this, -1);
            user.setApplication(app);
        }
    }

    public void updateCompleteUser(OtherUser user) throws SQLException {
        //use prepared statement
        //language=SQL
        String sql = "UPDATE users SET n = ?, tagSize = ?, sendKey = ?, recieveKey = ?, sendIdx = ?, sendTag = ?, recieveIdx = ?, recieveTag = ?, pending = ? WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        ClientApplication app = user.getApplication();
        statement.setInt(1, app.getN());
        statement.setInt(2,app.getTagSize());
        statement.setBytes(3, app.getSharedKey().getEncoded());
        statement.setBytes(4, app.getOtherKey().getEncoded());
        statement.setInt(5, app.getIdx());
        statement.setBytes(6, app.getTag());
        statement.setInt(7, app.getOtherIdx());
        statement.setBytes(8, app.getOtherTag());
        statement.setBoolean(9, user.isPending());
        statement.setInt(10, user.getId());

        statement.executeUpdate();
    }

    public void updateClientApp(ClientApplication app) throws SQLException {
        if (app.getId() == -1) {
            return; // silent fail
        }
        //use prepared statement
        //language=SQL
        String sql = "UPDATE users SET n = ?, tagSize = ?, sendKey = ?, recieveKey = ?, sendIdx = ?, sendTag = ?, recieveIdx = ?, recieveTag = ? WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, app.getN());
        statement.setInt(2,app.getTagSize());

        byte[]sendKeyBytes = null;
        byte[]recieveKeyBytes = null;
        if(app.getSharedKey() != null){
            sendKeyBytes = app.getSharedKey().getEncoded();
        }
        if(app.getOtherKey() != null){
            recieveKeyBytes = app.getOtherKey().getEncoded();
        }


        statement.setBytes(3, sendKeyBytes);
        statement.setBytes(4, recieveKeyBytes);
        statement.setInt(5, app.getIdx());
        statement.setBytes(6, app.getTag());
        statement.setInt(7, app.getOtherIdx());
        statement.setBytes(8, app.getOtherTag());
        statement.setInt(9, app.getId());

        statement.executeUpdate();
    }

    public void addMessage(OtherUser user, Message message) throws SQLException {
        //use prepared statement
        //language=SQL
        String sql = "INSERT INTO messages (content, sendByMe, sendAt, other_user_id) VALUES (?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, message.content);
        statement.setBoolean(2, message.sendByMe);
        statement.setTimestamp(3, Timestamp.valueOf(message.sendTime.toLocalDateTime()));
        statement.setInt(4, user.getId());

        statement.executeUpdate();
        message.setId(statement.getGeneratedKeys().getInt(1));
    }

    public List<Message> getMessages(OtherUser user) throws SQLException {
        //language=SQL
        String sql = "SELECT id, content, sendByMe, sendAt FROM messages WHERE other_user_id = ? ORDER BY sendAt ASC";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, user.getId());
        ResultSet rs = statement.executeQuery();
        List<Message> messages = new ArrayList<>();
        while (rs.next()) {
            int id = rs.getInt("id");
            String content = rs.getString("content");
            boolean sendByMe = rs.getBoolean("sendByMe");
            LocalDateTime sendAt = rs.getTimestamp("sendAt").toLocalDateTime();
            messages.add(new Message(content, ZonedDateTime.of(sendAt, ZoneId.systemDefault()), sendByMe, id));
        }
        return messages;
    }

    public void deleteUser(OtherUser user) throws SQLException {
        //language=SQL
        String sql = "DELETE FROM users WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, user.getId());
        statement.executeUpdate();
    }
}