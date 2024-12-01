import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * bulletin-board: OtherUser
 *
 * @author robbe
 * @version 16/11/2024
 */

public class OtherUser {
    private ClientApplication application;
    private String username;
    private List<Message> messages;

    private boolean pending;

    private Database database;
    private LocalDateTime addedAt;

    private boolean initialized = false; //when not initalized only name, id and pending!

    private int backupId;

    public OtherUser(ClientApplication application, String username, List<Message> messages, boolean pending, int id, Database database, boolean initialized, LocalDateTime addedAt) {
        this.application = application;
        this.username = username;
        this.messages = messages;
        this.pending = pending;
        this.database = database;
        this.initialized = initialized;
        this.addedAt = addedAt;
        this.backupId = id;
        if (application != null) {
            application.setId(id);
        }
    }

    @Override
    public String toString() {
        if (username == null) {
            return "Unknown user (added at: " + getFormattedAddedAt() + ")";
        } else {
            return username;
        }
    }

    public String getFormattedAddedAt() {
        String formatted = "";
        formatted += addedAt.getDayOfMonth() + "/" + addedAt.getMonthValue() + "/" + addedAt.getYear();
        formatted += " " + addedAt.getHour() + ":" + addedAt.getMinute();
        return formatted;
    }

    public static OtherUser createPendingRecieverUser(BulletinBoard board, Database db) throws NoSuchAlgorithmException, RemoteException, SQLException {
        ClientApplication application = new ClientApplication(new SecureRandom().generateSeed(32), board, db, -1);

        OtherUser user = new OtherUser(application, null, new ArrayList<>(), true, -1, db, true, LocalDateTime.now());
        return user;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }


    public String getUsername() {
        return username;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public int getId() {
        if (application != null) {
            return application.getId();
        } else {
            return backupId;
        }
    }

    public boolean isPending() {
        return pending;
    }

    public void setId(int id) {
        backupId = id;
        if (application != null) {
            application.setId(id);
        }
    }

    public void setUsername(String username) throws SQLException {
        this.username = username;
        database.updateUsername(this, username);
    }

    public void initialise(BulletinBoard board) throws SQLException, RemoteException {
        if (!initialized) {
            try {
                database.populateUser(this, board);
                initialized = true;
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    // also generates base64
    public String createReciever() throws Exception {
        KeyTransferDTO dto = this.application.generateKeyTransferDTOForOtherParty();
        byte[] dtoBytes = ObjectSerializer.serialize(dto);
        String base64String = Base64.getEncoder().encodeToString(dtoBytes);
        System.out.println("Base64: " + base64String);
        this.database.addRecieverUser(this);
        return base64String;
    }

    public ClientApplication getApplication() {
        return application;
    }

    public void setApplication(ClientApplication application) {
        this.application = application;
        application.setId(backupId);
    }

    public static OtherUser createFromBase64(String base64, BulletinBoard board, Database db) throws Exception {
        // deserialize the base64 string to a key transfer dto
        byte[] dtoBytes = Base64.getDecoder().decode(base64);
        KeyTransferDTO dto = ObjectSerializer.deserialize(dtoBytes);


        ClientApplication application = new ClientApplication(new SecureRandom().generateSeed(32), board, db, -1);
        application.receiveKeyTransferDTO(dto);
        OtherUser user = new OtherUser(application, null, new ArrayList<>(), false, -1, db, true, LocalDateTime.now());
        db.addCompleteUser(user);
        return user;
    }

    // returns true if the used changed fundamentally
    public boolean updateMessages() {
        boolean shouldUpdate = false;
        try {

            RawMessage data = null;
            do {
                data = application.receiveRawMessage(database.getHash(this), pending);
                if (data == null) {
                    break;
                }
                if (data.type == MessageType.MESSAGE) {
                    Message newMessage = Message.fromDTO(data.toMessageDTO());
                    database.addMessage(this, newMessage);
                    messages.add(newMessage);
                } else if (data.type == MessageType.INIT) {
                    KeyTransferDTO dto = data.toKeyTransferDTO();
                    this.application.initalizeSenderPartFromDto(dto);
                    this.pending = false;
                    database.updateCompleteUser(this);
                    shouldUpdate = true;
                } else {
                    throw new RuntimeException("Unknown message type");
                }
            } while(true);
            return shouldUpdate;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(Message message) throws Exception {

        // before sending message, check if the user is corrupted
        String databaseHash = database.getHash(this);

        this.messages.add(message);
        database.addMessage(this, message);
        this.application.sendRawMessage(RawMessage.fromMessageDTO(message.toDTO()), databaseHash, pending);
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}