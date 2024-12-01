import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

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

    private Database database;

    private int id;

    private boolean isCorrupted = false;


    // contains everything!
    public ClientApplication(byte[] seed, Key sharedKey, Key otherKey, int idx, byte[] tag, int otherIdx, byte[] otherTag, BulletinBoard board, Database db, int id) throws RemoteException {
        this.random = new SecureRandom(seed);
        ConnectionParams params = board.getConnectionParams();
        this.n = params.n;
        this.tagSize = params.tagSize;
        this.sharedKey = sharedKey;
        this.otherKey = otherKey;
        this.idx = idx;
        this.tag = tag;
        this.otherIdx = otherIdx;
        this.otherTag = otherTag;
        this.board = board;
        this.database = db;
        this.id = id;

    }

    public ClientApplication(byte[] seed, BulletinBoard board, Database db, int id) throws RemoteException {
        // most things are not initialized yet
        this.random = new SecureRandom(seed);
        ConnectionParams params = board.getConnectionParams();
        this.n = params.n;
        this.tagSize = params.tagSize;
        this.board = board;
        this.database = db;
        this.id = id;

    }


    public ClientApplication(byte[] seed, Key intialKey, int initialIdx, byte[] initialTag, BulletinBoard board, Database db, int id) throws RemoteException {
        this.random = new SecureRandom(seed);
        ConnectionParams params = board.getConnectionParams();
        this.n = params.n;
        this.tagSize = params.tagSize;
        this.sharedKey = intialKey;
        this.idx = initialIdx;
        this.tag = initialTag;
        this.board = board;
        this.database = db;
        this.id = id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    // get hash returns a hash made from sharedKey, idx, tag, otherKey, otherIdx, otherTag
    private String getHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Add idx as 4 bytes
            digest.update(ByteBuffer.allocate(4).putInt(idx).array());

            // Add tag bytes if not null
            if (tag != null) digest.update(tag);

            // Add shared key bytes if not null
            if (sharedKey != null) digest.update(sharedKey.getEncoded());

            // Add other key bytes if not null
            if (otherKey != null) digest.update(otherKey.getEncoded());

            // Add otherIdx as 4 bytes
            digest.update(ByteBuffer.allocate(4).putInt(otherIdx).array());

            // Add otherTag bytes if not null
            if (otherTag != null) digest.update(otherTag);

            // Return the Base64-encoded hash
            return Base64.getEncoder().encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to compute hash", e);
        }
    }


    // generate a base64 so another client can initiate a connection
    // this contains the initial key, the initial idx and the initial tag
    public KeyTransferDTO generateKeyTransferDTOForOtherParty() throws Exception {
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
        random.nextBytes(otherTag);
        database.updateClientApp(this);
        return new KeyTransferDTO(((SecretKeySpec) otherKey).getEncoded(), otherIdx, otherTag);
    }

    // what to do upon receiving a base64 from another client
    // this will set the initial key, the initial idx and the initial tag
    // we then generate a key, idx and tag for the other user
    // we then send a message to the other user containing this info
    public void receiveKeyTransferDTO(KeyTransferDTO dto) {

        byte[] keyBytes = dto.key;
        int idx = dto.idx;
        byte[] tag = dto.tag;

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

            // generate key transfer dto and send it
            KeyTransferDTO keyTransferDTO = new KeyTransferDTO(((SecretKeySpec) otherKey).getEncoded(), otherIdx, otherTag);
            sendRawMessage(RawMessage.fromKeyTransferDTO(keyTransferDTO), null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendRawMessage(RawMessage message, String databaseHash, boolean pending) throws Exception {
        byte[] data = message.toByteArray();
        sendBytes(data, databaseHash, pending);
    }

    public RawMessage receiveRawMessage(String databaseHash, boolean pending) throws Exception {
        byte[] data = receiveBytes(databaseHash, pending);
        if (data == null) {
            return null;
        }
        return RawMessage.fromByteArray(data);
    }



    public void initalizeSenderPartFromDto(KeyTransferDTO dto) {
        byte[] keyBytes = dto.key;
        int idx = dto.idx;
        byte[] tag = dto.tag;
        this.sharedKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
        this.idx = idx;
        this.tag = tag;

        if (database != null) {
            try {
                database.updateClientApp(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /*
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
    }*/

    public static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    public void sendBytes(byte[] data, String databaseHash, boolean pending) throws Exception {

        // get hash
        String hash = getHash();
        if(!hash.equals(databaseHash) && databaseHash != null) {
            //System.out.println("user is corrupted");
            isCorrupted = true;
            System.out.println("failed hash check");
            return;
        }

        int idxNext = random.nextInt(n);
        byte[] tagNext = generateTag();
        //BoardContent content = new BoardContent(data, idxNext, tagNext, type);
        Cipher cipher = Cipher.getInstance("AES");

        // combine idx, tag and then data into one byte array
        byte[] idxBytes = intToByteArray(idxNext);
        byte[] combined = new byte[idxBytes.length + tagNext.length + data.length];
        System.arraycopy(idxBytes, 0, combined, 0, idxBytes.length);
        System.arraycopy(tagNext, 0, combined, idxBytes.length, tagNext.length);
        System.arraycopy(data, 0, combined, idxBytes.length + tagNext.length, data.length);


        cipher.init(Cipher.ENCRYPT_MODE, sharedKey);
        byte[] encrypted = cipher.doFinal(combined);

        MessageDigest hashDigest = MessageDigest.getInstance("SHA-256");
        byte[] tagHash = hashDigest.digest(tag);

        board.write(idx, encrypted, tagHash);
        this.idx = idxNext;
        this.tag = tagNext;
        rotateKey();

        database.updateClientApp(this);
    }


    /*
    public ReceiveData receive() throws Exception {
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

        return new ReceiveData(new String(content.message), content.type);
    }*/

    public static int byteArrayToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8 ) |
                ((bytes[3] & 0xFF));

    }

    public byte[] receiveBytes(String databaseHash, boolean pending) throws Exception {

        String hash = getHash();
        if(!hash.equals(databaseHash) && databaseHash != null && !pending) {
            //System.out.println("user is corrupted");
            isCorrupted = true;
            return null;
        }

        byte[] encrypted = board.get(otherIdx, otherTag);
        if (encrypted == null) {
            return null;
        }
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, otherKey);
        byte[] decrypted = cipher.doFinal(encrypted);

        // unravel byte array first is idx, then tag, then data

        byte[] idxBytes = new byte[Integer.BYTES];
        byte[] tag = new byte[tagSize];
        byte[] data = new byte[decrypted.length - Integer.BYTES - tagSize];

        System.arraycopy(decrypted, 0, idxBytes, 0, Integer.BYTES);
        System.arraycopy(decrypted, Integer.BYTES, tag, 0, tagSize);
        System.arraycopy(decrypted, Integer.BYTES + tagSize, data, 0, data.length);

        //BoardContent content = BoardContent.fromByteArray(decrypted, tagSize);
        this.otherIdx = byteArrayToInt(idxBytes);
        this.otherTag = tag;
        // TODO: probable issue rotate other guys key not ours
        rotateOtherKey();

        database.updateClientApp(this);

        return data;
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


    /*
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
    }*/

    public int getN() {
        return n;
    }

    public int getTagSize() {
        return tagSize;
    }

    public Key getOtherKey() {
        return otherKey;
    }

    public int getOtherIdx() {
        return otherIdx;
    }

    public byte[] getOtherTag() {
        return otherTag;
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

    public boolean canReceive() {
        return otherKey != null && otherTag != null;
    }

    public boolean isCorrupted() {
        return isCorrupted;
    }
}