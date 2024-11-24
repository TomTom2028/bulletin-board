import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
* Default (Template) Project: ${NAME}
* @author robbe
* @version 11/11/2024
*/public class ClientMain {

    public static void main(String[] args) throws Exception {
//        ClientMain first = createNewApplication("client.db", "Client 1");
//        ClientMain second = createNewApplication("client2.db", "Client 2");
//
//        first.run();
//        second.run();
        testFn();
    }


    Database database;
    BulletinBoard board;

    OtherUser selectedContact;

    String appTitle;


    public ClientMain(BulletinBoard board, Database database, String appTitle) {
        this.selectedContact = null;
        this.board = board;
        this.database = database;
        this.appTitle = appTitle;
    }


    public static ClientMain createNewApplication(String databasePath, String appTitle) throws SQLException, ClassNotFoundException {
        Database database = new Database(databasePath);
        BulletinBoard board = BoardFactory.getBulletinBoard();
        if (board == null) {
            throw new RuntimeException("Could not connect to the bulletin board!");
        }

        return new ClientMain(board, database, appTitle);
    }




    public void run() {
        JFrame frame = new JFrame(appTitle);
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

        try {
            contacts.addAll(database.getOtherUsers());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        MainWindow mainWindow = new MainWindow(selectedContact);
        ContactSelector contactSelector = new ContactSelector(contacts, board, database, new ContactSelectedCallback() {
            @Override
            public void contactSelected(OtherUser user) {
                selectedContact = user;
                try {
                    selectedContact.initialise(board);
                } catch (RemoteException | SQLException e) {
                    // show error in dialog
                    JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
                    e.printStackTrace();

                }
                System.out.println("Selected user: " + user);
                mainWindow.refresh(selectedContact);

            }
        });
        frame.add(contactSelector, gbc);
        mainWindow.setContactSelector(contactSelector);

        // Add Label
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.8; // Expand width
        gbc.weighty = 1;

        frame.add(mainWindow, gbc);

        frame.pack();
        frame.setSize(400, 400);
        frame.setVisible(true);
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

            // make second pair of users
            byte[] seed3 = new byte[32];
            hardCodedRandom.nextBytes(seed3);

            byte[] tag3 = new byte[32];
            hardCodedRandom.nextBytes(tag3);
            int idx3 = hardCodedRandom.nextInt(10000);

            byte[] seed4 = new byte[32];
            System.arraycopy(seed3, 0, seed4, 0, seed3.length);
            byte[] tag4 = new byte[32];
            System.arraycopy(tag3, 0, tag4, 0, tag3.length);
            int idx4 = idx3;

            // use a different key
            byte[] keyBytes2 = new byte[32];
            hardCodedRandom.nextBytes(keyBytes2);
            Key key2 = new SecretKeySpec(keyBytes2, 0, keyBytes2.length, "AES");

//            ClientApplication client2 = new ClientApplication(seed3, key2, idx3, tag3, board);
//            ClientApplication receiver2 = new ClientApplication(seed4, key2, idx4, tag4, board);
//
//            receiver2.receiveBase64(client2.generateBase64());
//            System.out.println("Client2: " + client2.receive());


            //client2.send("Hello world!", MessageType.MESSAGE);

            receiver.receiveBase64(client.generateBase64());
            System.out.println("Client: " + client.receive());
            client.send("Hello world!", MessageType.MESSAGE);
            client.send("Hello world!2", MessageType.MESSAGE);
            client.send("Hello world!3", MessageType.MESSAGE);
            client.send("Hello world!4", MessageType.MESSAGE);
            client.send("Hello world!5", MessageType.MESSAGE);
            client.send("Hello world!6", MessageType.MESSAGE);

            //System.out.println("Receiver2: " + receiver2.receive());


            System.out.println("Receiver: " + receiver.receive());
            System.out.println("Receiver: " + receiver.receive());
            System.out.println("Receiver: " + receiver.receive());
            System.out.println("Receiver: " + receiver.receive());
            System.out.println("Receiver: " + receiver.receive());
            System.out.println("Receiver: " + receiver.receive());

            client.send("a fox can walk!", MessageType.MESSAGE);
            System.out.println("Receiver: " + receiver.receive());
            System.out.println("Receiver: " + receiver.receive());


            receiver.send("a receiver can also send messages", MessageType.MESSAGE);
            System.out.println("Client: " + client.receive());

            receiver.send("a receiver can also send messages", MessageType.MESSAGE);

        }
    }
}