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
    Map<CellKey, byte[]> datasets;
    public BoardCell() {
        datasets = new HashMap<>();
    }
}


class CellKey  {
    public byte[] tagHash;
    public CellKey(byte[] tagHash) {
        this.tagHash = tagHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CellKey other = (CellKey) obj;
        if (this.tagHash.length != other.tagHash.length) {
            return false;
        }

        for (int i = 0; i < this.tagHash.length; i++) {
            if (this.tagHash[i] != other.tagHash[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        for (int i = 0; i < this.tagHash.length; i++) {
            hash = 31 * hash + this.tagHash[i];
        }
        return hash;
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
        board[idx].datasets.put(new CellKey(tagHash), data);
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
           CellKey cellKey = new CellKey(tagHash);
           byte[] value = cell.datasets.getOrDefault(cellKey, null);
           if (value != null) {
               cell.datasets.remove(cellKey);
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