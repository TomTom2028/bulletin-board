import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * bulletin-board: BulletinBoardImpl
 *
 * @author robbe
 * @version 11/11/2024
 */

class BoardCell {
    Map<byte[], byte[]> datasets;
    public BoardCell() {
        datasets = new HashMap<>();
    }
}




public class BulletinBoardImpl extends UnicastRemoteObject implements BulletinBoard {

    public static final int N = 10000;
    public static final int TAG_SIZE = 32;
    private BoardCell[] board = new BoardCell[N];

    protected BulletinBoardImpl() throws RemoteException {
        super();
        for(int i = 0; i < N; i++) {
            board[i] = new BoardCell();
        }
    }


    @Override
    public void write(int idx, byte[] data, byte[] tagHash) throws RemoteException {
        if (idx >= N || idx < 0) {
            return;
        }
        System.out.println("Writing to index " + idx);
        System.out.println("Data: " + new String(data));
        System.out.println("Tag: " + new String(tagHash));
        board[idx].datasets.put(data, tagHash);
    }

    @Override
    public byte[] get(int idx, byte[] tag) throws RemoteException {
       try {
           if (idx >= N || idx < 0) {
               return null;
           }

           BoardCell cell = board[idx];
           MessageDigest hashDigest = MessageDigest.getInstance("SHA-256");
           byte[] tagHash = hashDigest.digest(tag);
           byte[] value = cell.datasets.getOrDefault(tagHash, null);
           if (value != null) {
               cell.datasets.remove(tagHash);
           }
           return value;
       } catch (Exception e) {
           return null;
       }
    }

    @Override
    public ConnectionParams getConnectionParams() throws RemoteException {
        return new ConnectionParams(N, TAG_SIZE);
    }
}