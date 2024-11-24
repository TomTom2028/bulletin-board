import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private int id;

    private boolean pending;

    private Database database;
    private LocalDateTime addedAt;
    private LocalDateTime lastMessageAt; // TODO: implement!

    private boolean initialized = false; //when not initalized only name, id and pending!

    public OtherUser(ClientApplication application, String username, List<Message> messages, boolean pending, int id, Database database, boolean initialized, LocalDateTime addedAt) {
        this.id = id;
        this.application = application;
        this.username = username;
        this.messages = messages;
        this.pending = pending;
        this.database = database;
        this.initialized = initialized;
        this.addedAt = addedAt;
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
        ClientApplication application = new ClientApplication(new SecureRandom().generateSeed(32), board);

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
        return id;
    }

    public boolean isPending() {
        return pending;
    }

    public void setId(int id) {
        this.id = id;
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
            }
        }
    }

    // also generates base64
    public String createReciever() throws Exception {
        String base64String = this.application.generateBase64();
        this.database.addRecieverUser(this);
        return base64String;
    }

    public ClientApplication getApplication() {
        return application;
    }

    public void setApplication(ClientApplication application) {
        this.application = application;
    }

    public static OtherUser createFromBase64(String base64, BulletinBoard board, Database db) throws Exception {
        ClientApplication application = new ClientApplication(new SecureRandom().generateSeed(32), board);
        application.receiveBase64(base64);
        OtherUser user = new OtherUser(application, null, new ArrayList<>(), false, -1, db, true, LocalDateTime.now());
        db.addCompleteUser(user);
        return user;
    }

    // returns true if the used changed fundamentally
    public boolean updateMessages() {
        boolean shouldUpdate = false;
        try {

            ReceiveData data = null;
            do {
                data = application.receive();
                if (data == null) {
                    break;
                }
                if (data.type == MessageType.MESSAGE) {
                    messages.add(new Message(data.message, LocalDateTime.now().toString(), false));
                } else if (data.type == MessageType.INIT) {
                    this.application.initalizeSenderPartFromBase64(data.message);
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
        this.messages.add(message);
        application.send(message.content, MessageType.MESSAGE);
    }


}