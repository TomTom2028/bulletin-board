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
            byte[] keyBytes = new byte[32];
            hardCodedRandom.nextBytes(keyBytes);
            Key key = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");


            byte[] seed = new byte[32];
            hardCodedRandom.nextBytes(seed);

            byte[] tag = new byte[32];
            hardCodedRandom.nextBytes(tag);
            int idx = hardCodedRandom.nextInt(10000);


            byte[] seed2 = new byte[32];
            System.arraycopy(seed, 0, seed2, 0, seed.length);
            byte[] tag2 = new byte[32];
            System.arraycopy(tag, 0, tag2, 0, tag.length);
            int idx2 = idx;

            ClientApplication client = new ClientApplication(seed, key, idx, tag, board);



            ClientApplication receiver = new ClientApplication(seed2, key, idx2, tag2, board);
            client.send("Hello world!");

            System.out.println("Receiver: " + receiver.receive());
            System.out.println("Receiver: " + receiver.receive());
            client.send("a fox can walk!");
            System.out.println("Receiver: " + receiver.receive());
        }
    }
}