import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.security.Key;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
* Default (Template) Project: ${NAME}
* @author robbe
* @version 11/11/2024
*/public class ClientMain {

    public static void main(String[] args) throws Exception {
        ClientMain first = createNewApplication("client.db", "Client 1");
        ClientMain second = createNewApplication("client2.db", "Client 2");

        first.run();
        second.run();
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

        // Create and populate the list of contacts
        List<OtherUser> contacts = new ArrayList<>();
        try {
            contacts.addAll(database.getOtherUsers());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Create the main components
        MainWindow mainWindow = new MainWindow(selectedContact);
        ContactSelector contactSelector = new ContactSelector(contacts, board, database, new ContactSelectedCallback() {
            @Override
            public void contactSelected(OtherUser user) {
                selectedContact = user;
                try {
                    selectedContact.initialise(board);
                } catch (RemoteException | SQLException e) {
                    // Show error in dialog
                    JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
                    e.printStackTrace();
                }
                System.out.println("Selected user: " + user);
                mainWindow.refresh(selectedContact);
            }
        });
        mainWindow.setContactSelector(contactSelector);

        // Add ContactSelector to the layout
        gbc.gridx = 0; // First column
        gbc.gridy = 0;
        gbc.weightx = 0.2; // 20% width
        gbc.weighty = 1.0; // Full height
        gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
        frame.add(contactSelector, gbc);

        // Add MainWindow to the layout
        gbc.gridx = 1; // Second column
        gbc.gridy = 0;
        gbc.weightx = 0.8; // 80% width
        gbc.weighty = 1.0; // Full height
        gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
        frame.add(mainWindow, gbc);

        // Fix resizing behavior
        frame.setMinimumSize(new Dimension(400, 400)); // Optional: Set minimum size
        frame.setSize(800, 600); // Set initial size for better visibility
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

            ClientApplication client = new ClientApplication(seed, key, idx, tag, board, null, -1);



            ClientApplication receiver = new ClientApplication(seed2, key, idx2, tag2, board, null, -1);
            //receiver.receiveKeyTransferDTO(client.generateBase64());
           /*
            System.out.println("Client: " + client.receive());
            client.send("Hello world!", MessageType.MESSAGE);

            System.out.println("Receiver: " + receiver.receive());
            System.out.println("Receiver: " + receiver.receive());
            client.send("a fox can walk!", MessageType.MESSAGE);
            System.out.println("Receiver: " + receiver.receive());

            receiver.send("a receiver can also send messages", MessageType.MESSAGE);
            System.out.println("Client: " + client.receive());*/
        }
    }
}