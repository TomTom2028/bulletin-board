import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.rmi.RemoteException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
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
    private Key otherKey;

    private int idx;
    private byte[] tag;

    // temp solution when only 2 people messaging
    public int otherIdx;
    public byte[] otherTag;

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

    public ClientApplication(byte[] seed, Key intialKey, int initialIdx, byte[] initialTag, int n, int tagSize) throws RemoteException {
        this.random = new SecureRandom(seed);
        this.n = n;
        this.tagSize = tagSize;
        this.sharedKey = intialKey;
        this.idx = initialIdx;
        this.tag = initialTag;
        this.board = board;
    }

    // generate a base64 so another client can initiate a connection
    // this contains the initial key, the initial idx and the initial tag
    public String generateBase64() throws Exception {
        // make the otherkey, otheridx and othertag
        // send the otherkey, otheridx and othertag to the other client

        // generate aes key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        otherKey = keyGen.generateKey();

        // generate idx
        otherIdx = random.nextInt(n);

        // generate tag
        otherTag = new byte[tagSize];
        random.nextBytes(tag);

        System.out.println("Generating base64");
        String base64 = Base64.getEncoder().encodeToString(((SecretKeySpec) otherKey).getEncoded()) + " " + otherIdx + " " + Base64.getEncoder().encodeToString(otherTag);
        System.out.println("Generated base64: " + base64);
        return base64;
    }

    // what to do upon receiving a base64 from another client
    // this will set the initial key, the initial idx and the initial tag
    // we then generate a key, idx and tag for the other user
    // we then send a message to the other user containing this info
    public void receiveBase64(String base64) {
        String[] parts = base64.split(" ");
        byte[] keyBytes = Base64.getDecoder().decode(parts[0]);
        int idx = Integer.parseInt(parts[1]);
        byte[] tag = Base64.getDecoder().decode(parts[2]);
        this.sharedKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
        this.idx = idx;
        this.tag = tag;
        try {
            //rotateKey();

            // generate aes key
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            otherKey = keyGen.generateKey();

            // generate idx
            otherIdx = random.nextInt(n);

            // generate tag
            otherTag = new byte[tagSize];
            random.nextBytes(otherTag);

            // generate base64
            String newBase64 = Base64.getEncoder().encodeToString(((SecretKeySpec) otherKey).getEncoded()) + " " + otherIdx + " " + Base64.getEncoder().encodeToString(otherTag);


            send(newBase64, MessageType.INIT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void send (String message, MessageType type) throws Exception {

        int idxNext = random.nextInt(n);
        byte[] tagNext = generateTag();
        BoardContent content = new BoardContent(message.getBytes(), idxNext, tagNext, type);
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
        byte[] encrypted = board.get(otherIdx, otherTag);
        if (encrypted == null) {
            return null;
        }
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, otherKey);
        byte[] decrypted = cipher.doFinal(encrypted);
        BoardContent content = BoardContent.fromByteArray(decrypted, tagSize);
        this.otherIdx = content.idx;
        this.otherTag = content.tag;
        // TODO: probable issue rotate other guys key not ours
        rotateOtherKey();
        if(content.type == MessageType.INIT) {
            System.out.println("Received INIT");
            // get the key, idx and tag from the message base64
            String base64 = new String(content.message);
            String[] parts = base64.split(" ");
            byte[] keyBytes = Base64.getDecoder().decode(parts[0]);
            int idx = Integer.parseInt(parts[1]);
            byte[] tag = Base64.getDecoder().decode(parts[2]);
            this.sharedKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
            this.idx = idx;
            this.tag = tag;
            return "INIT SUCCES";
        }
        else {
            return new String(content.message);
        }
        // return content.message;
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

    private void rotateOtherKey() throws Exception {
        byte[] keyBytes = otherKey.getEncoded();
        SecureRandom randomForKey = SecureRandom.getInstance("SHA1PRNG");
        randomForKey.setSeed(keyBytes);

        byte[] newKeyBytes = new byte[keyBytes.length];
        randomForKey.nextBytes(newKeyBytes);
        otherKey = new SecretKeySpec(newKeyBytes,0, newKeyBytes.length, "AES");
    }


    public static ClientApplication createReciever(BulletinBoard board) throws RemoteException, NoSuchAlgorithmException {
        ConnectionParams params = board.getConnectionParams();
        SecureRandom random = new SecureRandom();
        byte[] seed = new byte[32];
        random.nextBytes(seed);
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        Key key = keyGen.generateKey();
        byte[] tag = new byte[params.tagSize];
        random.nextBytes(tag);
        int idx = random.nextInt(params.n);
        return new ClientApplication(seed, key, idx, tag, board);
    }

    public int getN() {
        return n;
    }

    public int getTagSize() {
        return tagSize;
    }

    public Key getSharedKey() {
        return sharedKey;
    }

    public int getIdx() {
        return idx;
    }

    public byte[] getTag() {
        return tag;
    }
}