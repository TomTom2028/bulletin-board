import javax.crypto.spec.SecretKeySpec;
import java.rmi.RemoteException;
import java.security.Key;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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

    public static OtherUser createOtherUserFromRecoveryString(String base64String, Database db, BulletinBoard board) throws RemoteException, SQLException {
        byte[] serialized = Base64.getDecoder().decode(base64String);
        RecoveryUserDTO recoveryUserDTO = ObjectSerializer.deserialize(serialized);
        KeyTransferDTO myKey = recoveryUserDTO.myKey;
        KeyTransferDTO otherKey = recoveryUserDTO.otherKey;
        int idx = myKey.idx;
        byte[] tag = myKey.tag;
        Key sharedKey = new SecretKeySpec(myKey.key, 0, myKey.key.length, "AES");

        int otherIdx = otherKey.idx;
        byte[] otherTag = otherKey.tag;
        Key otherSharedKey = new SecretKeySpec(otherKey.key, 0, otherKey.key.length, "AES");
        byte[] seed = new SecureRandom().generateSeed(32);
        ClientApplication application = new ClientApplication(seed, sharedKey, otherSharedKey, idx, tag, otherIdx, otherTag, board, db, -1);
        List<Message> messages = new ArrayList<>();
        for (RecoveryMessageDTO messageDTO : recoveryUserDTO.messages) {
            messages.add(Message.fromRecoveryDTO(messageDTO));
        }

        OtherUser user = new OtherUser(application, null, messages, false, -1, db, true, LocalDateTime.now());
        db.addCompleteUser(user);
        for (Message message : messages) {
            db.addMessage(user, message);
        }
        return user;
    }
}