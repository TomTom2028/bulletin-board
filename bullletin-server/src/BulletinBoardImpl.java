import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.util.Arrays;
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

    public static int N = 1;
    public static final int TAG_SIZE = 32;
    private BoardCell[] board = new BoardCell[N];
    private BoardCell[] transitionBoard = null;
    private int delta = 0;

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

        if (transitionBoard != null && idx < transitionBoard.length) {
            // If resizing, write to the transition board.
            transitionBoard[idx].datasets.put(new CellKey(tagHash), data);
        } else {
            // Regular write to the main board.
            board[idx].datasets.put(new CellKey(tagHash), data);
        }
        delta++; // Increment the message count.
        checkResize(); // Check if resizing is needed.
    }

    @Override
    public synchronized byte[] get(int idx, byte[] tag) throws RemoteException {
        try {
            if (idx < 0) {
                System.out.println("Invalid index.");
                return null;
            }

            MessageDigest hashDigest = MessageDigest.getInstance("SHA-256");
            byte[] tagHash = hashDigest.digest(tag);
            CellKey cellKey = new CellKey(tagHash);

            // Check both boards during transition.
            byte[] value = null;
            if (board[idx] != null) {
                value = board[idx].datasets.remove(cellKey);
            }
            if (value == null && transitionBoard != null && idx < transitionBoard.length) {
                System.out.println("Checking transition board.");
                value = transitionBoard[idx].datasets.remove(cellKey);
            }

            if (value != null) {
                delta--; // Decrement the message count.
                //checkResize(); // Check if resizing is needed.
            }
            return value;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isBoardEmpty(BoardCell[] boardToCheck) {
        if (boardToCheck == null) {
            return true;
        }
        for (BoardCell cell : boardToCheck) {
            if (cell != null && !cell.datasets.isEmpty()) {
                return false; // Board is not empty if any dataset is non-empty.
            }
        }
        return true; // All cells are empty.
    }

    private synchronized void checkResize() {
        // If board is too full, create a larger transition board.
        if (delta > N * 0.8 && transitionBoard == null) {
            startResize(N * 2);
        }
        // If board is too empty, create a smaller transition board.
        if (delta < N * 0.2 && transitionBoard == null) {
            startResize(N / 2);
        }

        // Clean up old board if it is empty.
        // Clean up old board if it is actually empty.
        if (transitionBoard != null && isBoardEmpty(board)) {
            System.out.println("Cleaning up old board.");
            board = transitionBoard;
            transitionBoard = null;
            N = board.length;
        }
    }

    private void startResize(int newSize) {
        System.out.println("Resizing board to size: " + newSize);
        transitionBoard = new BoardCell[newSize];
        for (int i = 0; i < newSize; i++) {
            transitionBoard[i] = new BoardCell();
        }
    }


    @Override
    public ConnectionParams getConnectionParams() throws RemoteException {
        int currentBoardSize = (transitionBoard != null) ? transitionBoard.length : N;
        boolean resizing = transitionBoard != null;
        boolean resizingUp = transitionBoard != null && transitionBoard.length > N;
        return new ConnectionParams(currentBoardSize, TAG_SIZE, resizing, resizingUp);
    }
}