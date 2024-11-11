import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * bulletin-board: BoardFactory
 *
 * @author robbe
 * @version 11/11/2024
 */

public class BoardFactory {
    public static BulletinBoard getBulletinBoard() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            return (BulletinBoard) registry.lookup("BulletinBoard");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}