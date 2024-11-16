import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.HashMap;

/**
 * bulletin-board: ContactSelector
 *
 * @author robbe
 * @version 16/11/2024
 */




public class ContactSelector extends JPanel {

    private ContactPanel contactPanel;
    HashMap<String, OtherUser> contacts;


    private class ContactPanel extends JPanel {
        public ContactPanel() {
            super();

            if (contacts.isEmpty()) {
                setLayout(new GridLayout());
                JLabel notFound = new JLabel("No contacts found");
                notFound.setHorizontalAlignment(JLabel.CENTER);
                add(notFound) ;
            } else {
                JPanel contactPanel = new JPanel();
                contactPanel.setLayout(new BoxLayout(contactPanel, BoxLayout.Y_AXIS));

                for (String key : contacts.keySet()) {
                    contactPanel.add(new JLabel(key));
                }
                JScrollPane scrollPane = new JScrollPane(contactPanel);
                add(scrollPane);
            }
        }
    }






    public ContactSelector(HashMap<String, OtherUser> contacts)
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
                    contacts.put(userString, new OtherUser(null, null, "bob", null, null));
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