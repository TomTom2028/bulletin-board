import java.rmi.RemoteException;

/**
* Default (Template) Project: ${NAME}
* @author robbe
* @version 11/11/2024
*/public class ClientMain {
    public static void main(String[] args) throws RemoteException {
        System.out.println("Hello world!");
        BulletinBoard board = BoardFactory.getBulletinBoard();
        if(board != null) {
            board.post("Hello, bulletin board!");
            System.out.println(board.read());
        }
    }
}