import java.io.Serializable;

/**
 * bulletin-board: KeyTransferDTO
 *
 * @author robbe
 * @version 30/11/2024
 */

public class KeyTransferDTO implements Serializable {
    public byte[] key;
    public int idx;
    public byte[] tag;

    public KeyTransferDTO(byte[] key, int idx, byte[] tag) {
        this.key = key;
        this.idx = idx;
        this.tag = tag;
    }

}