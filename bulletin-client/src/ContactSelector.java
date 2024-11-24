import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * bulletin-board: ContactSelector
 *
 * @author robbe
 * @version 16/11/2024
 */





public class ContactSelector extends JPanel {

    private ContactPanel contactPanel;
    List<OtherUser> contacts;
    ContactSelectedCallback callback;


    private class ContactPanel extends JPanel {
        public ContactPanel() {
            super();
            setPreferredSize(new Dimension(200, 20));

            if (contacts.isEmpty()) {
                setLayout(new GridLayout());
                JLabel notFound = new JLabel("No contacts found");
                notFound.setHorizontalAlignment(JLabel.CENTER);
                add(notFound) ;
            } else {
                setLayout(new BorderLayout ());
                JList<OtherUser> contactList = new JList<>(contacts.toArray(new OtherUser[0]));
                contactList.addListSelectionListener(e -> {
                    callback.contactSelected(contactList.getSelectedValue());
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



    public ContactSelector(List<OtherUser> contacts, BulletinBoard board, Database db, ContactSelectedCallback callback)
    {
        super();
        this.callback = callback;
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setLayout(new GridBagLayout());
        // make boxlayout fill all available space
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
                    // show success dialog
                    JOptionPane.showMessageDialog(this, "Contact added successfully");
                    //contacts.add(new OtherUser(null, null, "bob", null, userString));
                   refreshALl();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    // show error in dialog
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
                //TODO: make this show in a dialog or somehting like that
                JOptionPane.showMessageDialog(this, "Base64: " + base64);
                refreshALl();
            } catch (Exception ex) {
                // show error in dialog
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

    }
}