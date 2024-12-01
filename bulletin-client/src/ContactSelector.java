import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.util.List;

public class ContactSelector extends JPanel {

    private ContactPanel contactPanel;
    List<OtherUser> contacts;
    ContactSelectedCallback callback;
    private OtherUser selectedUser;
    private JButton createRecoveryKeyBtn;
    private JButton deleteContactBtn;


    private class ContactPanel extends JPanel {
        public ContactPanel() {
            super();
            setPreferredSize(new Dimension(50, 20));

            if (contacts.isEmpty()) {
                setLayout(new GridLayout());
                JLabel notFound = new JLabel("No contacts found");
                notFound.setHorizontalAlignment(JLabel.CENTER);
                add(notFound);
            } else {
                setLayout(new BorderLayout());
                JList<OtherUser> contactList = new JList<>(contacts.toArray(new OtherUser[0]));
                contactList.addListSelectionListener(e -> {
                    selectedUser = contactList.getSelectedValue();
                    callback.contactSelected(selectedUser);
                    createRecoveryKeyBtn.setEnabled(selectedUser != null);
                    deleteContactBtn.setEnabled(selectedUser != null);
                });
                contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                contactList.setLayoutOrientation(JList.VERTICAL);
                ScrollPane scrollPane = new ScrollPane();
                scrollPane.add(contactList);
                add(scrollPane);
            }
        }
    }

    private GridBagConstraints contactsGbc = new GridBagConstraints();

    public void refreshALl() {
        remove(this.contactPanel);
        this.contactPanel = new ContactPanel();
        add(this.contactPanel, contactsGbc);
        revalidate();
        repaint();
    }

    public ContactSelector(List<OtherUser> contacts, BulletinBoard board, Database db, ContactSelectedCallback callback) {
        super();
        this.callback = callback;
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setLayout(new GridBagLayout());
        setBackground(Color.LIGHT_GRAY);
        this.contacts = contacts;
        this.contactPanel = new ContactPanel();

        contactsGbc.weightx = 1;
        contactsGbc.weighty = 0.8;
        contactsGbc.gridx = 0;
        contactsGbc.gridy = 0;
        contactsGbc.fill = GridBagConstraints.BOTH;

        add(this.contactPanel, contactsGbc);

        JButton addContact = new JButton("Add contact");
        addContact.addActionListener(e -> {
            String userString = JOptionPane.showInputDialog("Enter base64 contact string");
            if (userString != null) {
                try {
                    OtherUser user = OtherUser.createFromBase64(userString, board, db);
                    contacts.add(user);
                    JOptionPane.showMessageDialog(this, "Contact added successfully");
                    refreshALl();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 0.1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(addContact, gbc);

        JButton createBase64 = new JButton("Create base64 to share");
        createBase64.addActionListener(e -> {
            try {
                OtherUser pendingUser = OtherUser.createPendingRecieverUser(board, db);
                contacts.add(pendingUser);
                String base64 = pendingUser.createReciever();

                // Copy to clipboard
                StringSelection selection = new StringSelection(base64);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, null);

                // Notify user of successful copy
                JOptionPane.showMessageDialog(this, "Base64 copied to clipboard!");
                refreshALl();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
        gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 0.1;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        add(createBase64, gbc);

        deleteContactBtn = new JButton("Remove contact");
        deleteContactBtn.addActionListener(e -> {
            try {
                db.deleteUser(selectedUser);
                contacts.remove(selectedUser);
                callback.contactSelected(null);
                JOptionPane.showMessageDialog(this, "Contact removed successfully");
                refreshALl();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
        gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 0.1;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        add(deleteContactBtn, gbc);

        createRecoveryKeyBtn = new JButton("Create recovery key for other user");
        createRecoveryKeyBtn.addActionListener(e -> {
            try {
                String recoveryString = Recovery.createRestoreStringForOtherUser(selectedUser);
                StringSelection selection = new StringSelection(recoveryString);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, null);

                // Notify user of successful copy
                JOptionPane.showMessageDialog(this, "Recovery key copied to clipboard!");

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
        gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 0.1;
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.BOTH;
        add(createRecoveryKeyBtn, gbc);


        if (selectedUser == null) {
            createRecoveryKeyBtn.setEnabled(false);
            deleteContactBtn.setEnabled(false);
        }


        JButton recoverFromRecoveryKeyBtn = new JButton("Recover from recovery key of other user");
        gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 0.1;
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.BOTH;
        add(recoverFromRecoveryKeyBtn, gbc);

        recoverFromRecoveryKeyBtn.addActionListener(e -> {
            try {
                String recoveryString = JOptionPane.showInputDialog("Enter recovery key");
                if (recoveryString != null) {
                    recoveryString = recoveryString.trim();
                    OtherUser user = Recovery.createOtherUserFromRecoveryString(recoveryString, db, board);
                    contacts.add(user);
                    JOptionPane.showMessageDialog(this, "User recovered successfully");
                    refreshALl();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
    }
}
