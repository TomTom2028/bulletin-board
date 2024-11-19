import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
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
    private ClientApplication senderApp;
    private ClientApplication recieverApp;

    private String username;
    private List<Message> messages;
    private int id;

    private boolean pending;

    private Database database;
    private LocalDateTime addedAt;
    private LocalDateTime lastMessageAt; // TODO: implement!

    private boolean initialized = false; //when not initalized only name, id and pending!

    public OtherUser(ClientApplication senderApp, ClientApplication recieverApp, String username, List<Message> messages, boolean pending, int id, Database database, boolean initialized, LocalDateTime addedAt) {
        this.id = id;
        this.senderApp = senderApp;
        this.recieverApp = recieverApp;
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

    public static OtherUser createPendingUser(BulletinBoard board, Database db) throws NoSuchAlgorithmException, RemoteException, SQLException {
        ClientApplication reciever = ClientApplication.createReciever(board);

        OtherUser user = new OtherUser(null, reciever, null, new ArrayList<>(), true, -1, db, true, LocalDateTime.now());
        db.addRecieverUser(user);
        return user;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public ClientApplication getSenderApp() {
        return senderApp;
    }

    public ClientApplication getRecieverApp() {
        return recieverApp;
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

    public void setSenderApp(ClientApplication senderApp) {
        this.senderApp = senderApp;
    }
    public void setRecieverApp(ClientApplication recieverApp) {
        this.recieverApp = recieverApp;
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
}