import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
* Default (Template) Project: ${NAME}
* @author robbe
* @version 11/11/2024
*/public class ClientMain {

    public ClientMain() {

    }

    public void run() {
        JFrame frame = new JFrame("whatsapp 2");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.EAST; // Ensure proper alignment

        // Add ContactSelector
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2; // Reset gridwidth
        gbc.weighty = 1;


        gbc.anchor = GridBagConstraints.CENTER; // Ensure proper alignment
        gbc.fill = GridBagConstraints.BOTH;
        List<OtherUser> contacts = new ArrayList<>();
        frame.add(new ContactSelector(contacts), gbc);

        // Add Label
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.8; // Expand width
        gbc.weighty = 1;

        JLabel label = new JLabel("Hello world!");
        frame.add(label, gbc);

        frame.pack();
        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        ClientMain clientMain = new ClientMain();
        clientMain.run();
    }


    private static void testFn() throws Exception {
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
            receiver.receiveBase64(client.generateBase64());
            System.out.println("Client: " + client.receive());
            client.send("Hello world!", MessageType.MESSAGE);

            System.out.println("Receiver: " + receiver.receive());
            System.out.println("Receiver: " + receiver.receive());
            client.send("a fox can walk!", MessageType.MESSAGE);
            System.out.println("Receiver: " + receiver.receive());

            receiver.send("a receiver can also send messages", MessageType.MESSAGE);
            System.out.println("Client: " + client.receive());
        }
    }
}