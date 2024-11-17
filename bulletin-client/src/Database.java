import java.sql.*;

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
                "pending BOOLEAN" +
                ");");

    }


    private void runStatement(String statement) throws SQLException {
        connection.createStatement().execute(statement);
    }

    public void addRecieverUser(OtherUser user) throws SQLException {
        //use prepared statement
        //language=SQL
        String sql = "INSERT INTO users (username, n, tagSize, recieveKey, recieveIdx, recieveTag, pending) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, user.getUsername());
        statement.setInt(2, user.getRecieverApp().getN());
        statement.setInt(3, user.getRecieverApp().getTagSize());
        statement.setBytes(4, user.getRecieverApp().getSharedKey().getEncoded());
        statement.setInt(5, user.getRecieverApp().getIdx());
        statement.setBytes(6, user.getRecieverApp().getTag());
        statement.setBoolean(7, user.isPending());


        statement.executeUpdate();
        user.setId(statement.getGeneratedKeys().getInt(1));

    }

}