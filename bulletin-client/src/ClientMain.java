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
//
        first.run();
        second.run();
        //testFn();
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
                if (selectedContact == null) {
                    mainWindow.refresh(null);
                    return;
                }
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
}