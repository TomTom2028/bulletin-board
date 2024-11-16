import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * bulletin-board: BulletinBoard
 *
 * @author robbe
 * @version 11/11/2024
 */
public interface BulletinBoard extends Remote {

    public void write(int idx, byte[] data, byte[] tagHash) throws RemoteException;
    public byte[] get(int idx, byte[] tag) throws RemoteException;

    public ConnectionParams getConnectionParams() throws RemoteException;

}
