import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * bulletin-board: ContactSelector
 *
 * @author robbe
 * @version 16/11/2024
 */


class UserListModel extends AbstractListModel<String> {
    private List<OtherUser> contacts;

    public UserListModel(List<OtherUser> contacts) {
        this.contacts = contacts;
    }

    @Override
    public int getSize() {
        return contacts.size();
    }

    @Override
    public String getElementAt(int index) {
        return contacts.get(index).getUsername();
    }
}



public class ContactSelector extends JPanel {

    private ContactPanel contactPanel;
    List<OtherUser> contacts;


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
                    System.out.println("Selected: " + contactList.getSelectedValue().getUuid());
                });
                contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                contactList.setLayoutOrientation(JList.VERTICAL);
                ScrollPane scrollPane = new ScrollPane();
                scrollPane.add(contactList);
                add(scrollPane);
            }


        }
    }






    public ContactSelector(List<OtherUser> contacts)
    {
        super();
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setLayout(new GridBagLayout());
        // make boxlayout fill all available space
        setBackground(Color.LIGHT_GRAY);
        this.contacts = contacts;
        this.contactPanel = new ContactPanel();

        GridBagConstraints contactsGbc = new GridBagConstraints();
        contactsGbc.weightx = 1;
        contactsGbc.weighty = 0.8;
        contactsGbc.gridx = 0;
        contactsGbc.gridy = 0;
        contactsGbc.fill = GridBagConstraints.BOTH;
        add(this.contactPanel, contactsGbc);

        JButton addContact = new JButton("Add contact");
        addContact.addActionListener(e -> {
            String userString = JOptionPane.showInputDialog("Enter base64 contaxt string");
            if (userString != null) {
                try {
                    contacts.add(new OtherUser(null, null, "bob", null, userString));
                    System.out.println("Added contact");
                    // remvoe and re add
                    remove(this.contactPanel);
                    this.contactPanel = new ContactPanel();
                    add(this.contactPanel, contactsGbc);
                    revalidate();
                    repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 0.2;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(addContact, gbc);

    }
}