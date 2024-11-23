import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * bulletin-board: MainWindow
 *
 * @author robbe
 * @version 19/11/2024
 */






public class MainWindow extends JPanel {
    private OtherUser selectedUser;
    private InternalMainWindow internalMainWindow;
    ContactSelector contactSelector;
    public MainWindow(OtherUser selectedUser) {
        super();
        this.selectedUser = selectedUser;
        setLayout(new BorderLayout());
        this.internalMainWindow = new InternalMainWindow();
        add(this.internalMainWindow, BorderLayout.CENTER);
    }

    public void setContactSelector(ContactSelector contactSelector) {
        this.contactSelector = contactSelector;
    }


    private class InternalMainWindow extends JPanel {
        public InternalMainWindow() {
            super();
            if (selectedUser == null) {
                setLayout(new BorderLayout());
                JLabel noUser = new JLabel("No user selected");
                noUser.setHorizontalAlignment(JLabel.CENTER);
                add(noUser);
            } else {
                boolean isPending = selectedUser.isPending();

                // Set the layout
                setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(5, 5, 5, 5); // Margins between components

                // Top section: Name label and Edit button
                JLabel nameLabel = new JLabel(selectedUser.toString());
                JButton editNameButton = new JButton("Edit Name");
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.gridwidth = 1;
                gbc.anchor = GridBagConstraints.WEST;
                add(editNameButton, gbc);

                gbc.gridx = 1;
                gbc.gridy = 0;
                gbc.gridwidth = 1;
                gbc.anchor = GridBagConstraints.EAST;
                add(nameLabel, gbc);

                // Middle section: ScrollPane with messages


                JTextArea messageArea = new JTextArea(20, 30);
                messageArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(messageArea);
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.gridwidth = 2;
                gbc.fill = GridBagConstraints.BOTH;
                gbc.weightx = 1.0;
                gbc.weighty = 1.0;
                add(scrollPane, gbc);
                if (isPending) {
                    messageArea.setText("This user is pending");
                } else {
                    // TODO! Add messages
                }

                // Bottom section: TextField and Send button
                JTextField messageField = new JTextField(20);
                JButton sendButton = new JButton("Send");
                if (isPending) {
                    messageField.setEnabled(false);
                    sendButton.setEnabled(false);
                }
                gbc.gridx = 0;
                gbc.gridy = 2;
                gbc.gridwidth = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 0.8;
                gbc.weighty = 0.0;
                add(messageField, gbc);

                gbc.gridx = 1;
                gbc.gridy = 2;
                gbc.gridwidth = 1;
                gbc.fill = GridBagConstraints.NONE;
                gbc.weightx = 0.2;
                add(sendButton, gbc);


                editNameButton.addActionListener(e -> {
                    String newName = JOptionPane.showInputDialog("Enter new name");
                    if (newName != null) {
                        newName = newName.trim();
                        if (newName.length() < 2) {
                            JOptionPane.showMessageDialog(this, "Name must be at least 2 characters long");
                            return;
                        }
                        try {
                            selectedUser.setUsername(newName);
                            nameLabel.setText(newName);
                            if (contactSelector != null) {
                                contactSelector.refreshALl();
                            }
                        } catch (SQLException throwables) {
                            // show error dialog
                            JOptionPane.showMessageDialog(this, "Error: " + throwables.getMessage());
                            throwables.printStackTrace();
                        }
                    }
                });
            }
        }
    }


    public void refresh(OtherUser selectedUser) {
        this.selectedUser = selectedUser;
        remove(this.internalMainWindow);
        this.internalMainWindow = new InternalMainWindow();
        add(internalMainWindow, BorderLayout.CENTER);
        revalidate();
        repaint();
    }


}