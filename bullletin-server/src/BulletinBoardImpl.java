import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * bulletin-board: BulletinBoardImpl
 *
 * @author robbe
 * @version 11/11/2024
 */

public class BulletinBoardImpl extends UnicastRemoteObject implements BulletinBoard {
    protected BulletinBoardImpl() throws RemoteException {
        super();
    }

    @Override
    public void post(String message) {
        System.out.println("storing " + message);
    }

    @Override
    public String read() {
        return "read thingy";
    }
}