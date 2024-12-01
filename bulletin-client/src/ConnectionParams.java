import java.io.Serializable;

/**
 * bulletin-board: ConnectionParams
 *
 * @author robbe
 * @version 16/11/2024
 */

public class ConnectionParams implements Serializable {
    public int n;
    public int tagSize;
    public boolean resizing;
    public boolean resizingUp;

    public ConnectionParams(int n, int tagSize, boolean resizing, boolean resizingUp) {
        this.n = n;
        this.tagSize = tagSize;
        this.resizing = resizing;
        this.resizingUp = resizingUp;
    }
}