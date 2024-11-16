import javax.crypto.spec.SecretKeySpec;
import java.rmi.RemoteException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
* Default (Template) Project: ${NAME}
* @author robbe
* @version 11/11/2024
*/public class ClientMain {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello world!");
        BulletinBoard board = BoardFactory.getBulletinBoard();

        SecureRandom hardCodedRandom = SecureRandom.getInstance("SHA1PRNG");
        hardCodedRandom.setSeed(0xdeadbeefL);


        if(board != null) {
            // should all be made form different rng generators! (execpt the key)
            byte[] seed = new byte[32];
            hardCodedRandom.nextBytes(seed);
            byte[] keyBytes = new byte[32];
            hardCodedRandom.nextBytes(keyBytes);
            Key key = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
            byte[] tag = new byte[32];
            hardCodedRandom.nextBytes(tag);
            int idx = hardCodedRandom.nextInt(10000);
            ClientApplication client = new ClientApplication(seed, key, idx, tag, board);
            client.send("Hello world!");
            client.send("a fox can walk!");
        }
    }
}