import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

/**
* Default (Template) Project: ${NAME}
* @author robbe
* @version 11/11/2024
*/public class ServerMain {
    public static void main(String[] args) {
        try {
            System.out.println("Server starting...");
            Remote stub = new BulletinBoardImpl();
            Registry registry = java.rmi.registry.LocateRegistry.createRegistry(1099);
            registry.rebind("BulletinBoard", stub);
            System.out.println("Server started!");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}