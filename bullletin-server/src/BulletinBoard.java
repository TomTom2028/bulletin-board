import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * bulletin-board: BulletinBoard
 *
 * @author robbe
 * @version 11/11/2024
 */
public interface BulletinBoard extends Remote {
    void post(String message) throws RemoteException;
    String read() throws RemoteException;
}
