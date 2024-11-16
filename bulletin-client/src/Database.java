import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
                "username TEXT NOT NULL," +
                "n INTEGER NOT NULL," +
                "tagSize INTEGER NOT NULL," +
                "sharedKey BLOB," +
                "otherKey BLOB" +
                "idx INTEGER NOT NULL," +
                "tag BLOB NOT NULL," +
                "otherIdx INTEGER NOT NULL," +
                "otherTag BLOB NOT NULL" +
                ")");

    }


    private void runStatement(String statement) throws SQLException {
        connection.createStatement().execute(statement);
    }




}