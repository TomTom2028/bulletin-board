import java.util.Base64;

/**
 * bulletin-board: Backup
 *
 * @author robbe
 * @version 30/11/2024
 */

public class Recovery {
    public static String createRestoreStringForOtherUser(OtherUser user) {
        RecoveryMessageDTO[] messages = user.getMessages().stream().map(message ->
                new RecoveryMessageDTO(message.content, message.sendTime, !message.sendByMe)).toArray(RecoveryMessageDTO[]::new);

        if (user.getApplication().getSharedKey() == null || user.getApplication().getOtherKey() == null) {
            throw new IllegalArgumentException("First-time setup not completed for user!");
        }


        KeyTransferDTO myKey = new KeyTransferDTO(user.getApplication().getOtherKey().getEncoded(), user.getApplication().getOtherIdx(), user.getApplication().getOtherTag());
        KeyTransferDTO otherKey = new KeyTransferDTO(user.getApplication().getSharedKey().getEncoded(), user.getApplication().getIdx(), user.getApplication().getTag());
        RecoveryUserDTO recoveryUserDTO = new RecoveryUserDTO(myKey, otherKey, messages);
        byte[] serialized = ObjectSerializer.serialize(recoveryUserDTO);
        return Base64.getEncoder().encodeToString(serialized);
    }
}