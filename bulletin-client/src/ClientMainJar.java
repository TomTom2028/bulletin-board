import java.sql.SQLException;

/**
 * bulletin-board: JarMain
 *
 * @author robbe
 * @version 01/12/2024
 */

public class ClientMainJar {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        ClientMain clientMain = ClientMain.createNewApplication("client.db", "Whatsapp 2");
        clientMain.run();
    }
}