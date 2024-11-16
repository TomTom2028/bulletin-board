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

    public ConnectionParams(int n, int tagSize) {
        this.n = n;
        this.tagSize = tagSize;
    }
}