import java.io.Serializable;

/**
 * bulletin-board: RecoveryDTO
 *
 * @author robbe
 * @version 30/11/2024
 */

public class RecoveryUserDTO implements Serializable {
    public KeyTransferDTO myKey;
    public KeyTransferDTO otherKey;
    public RecoveryMessageDTO[] messages;

    public RecoveryUserDTO(KeyTransferDTO myKey, KeyTransferDTO otherKey, RecoveryMessageDTO[] messages) {
        this.myKey = myKey;
        this.otherKey = otherKey;
        this.messages = messages;
    }
}