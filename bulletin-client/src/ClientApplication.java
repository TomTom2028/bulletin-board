import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.rmi.RemoteException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * bulletin-board: ClientApplication
 *
 * @author robbe
 * @version 16/11/2024
 */

public class ClientApplication {

    SecureRandom random;
    private final int n;
    private final int tagSize;
    private Key sharedKey;

    private int idx;
    private byte[] tag;

    private BulletinBoard board;

    public ClientApplication(byte[] seed, Key intialKey, int initialIdx, byte[] initialTag, BulletinBoard board) throws RemoteException {
        this.random = new SecureRandom(seed);
        ConnectionParams params = board.getConnectionParams();
        this.n = params.n;
        this.tagSize = params.tagSize;
        this.sharedKey = intialKey;
        this.idx = initialIdx;
        this.tag = initialTag;
        this.board = board;
    }


    public void send (String message) throws Exception {

        int idxNext = random.nextInt(n);
        byte[] tagNext = generateTag();
        BoardContent content = new BoardContent(message, idxNext, tagNext);
        Cipher cipher = Cipher.getInstance("AES");


        cipher.init(Cipher.ENCRYPT_MODE, sharedKey);
        byte[] encrypted = cipher.doFinal(content.toByteArray());

        MessageDigest hashDigest = MessageDigest.getInstance("SHA-256");
        byte[] tagHash = hashDigest.digest(tag);

        board.write(idx, encrypted, tagHash);
        this.idx = idxNext;
        this.tag = tagNext;
        rotateKey();
    }


    public String receive() throws Exception {
        byte[] encrypted = board.get(idx, tag);
        if (encrypted == null) {
            return null;
        }
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, sharedKey);
        byte[] decrypted = cipher.doFinal(encrypted);
        BoardContent content = BoardContent.fromByteArray(decrypted, tagSize);
        this.idx = content.idx;
        this.tag = content.tag;
        rotateKey();
        return content.message;
    }


    private byte[] generateTag() {
        byte[] tag = new byte[tagSize];
        random.nextBytes(tag);
        return tag;
    }

    private void rotateKey() throws Exception {
        byte[] keyBytes = sharedKey.getEncoded();
        SecureRandom randomForKey = SecureRandom.getInstance("SHA1PRNG");
        randomForKey.setSeed(keyBytes);

        byte[] newKeyBytes = new byte[keyBytes.length];
        randomForKey.nextBytes(newKeyBytes);
        sharedKey = new SecretKeySpec(newKeyBytes,0, newKeyBytes.length, "AES");
    }
}