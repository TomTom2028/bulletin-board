import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
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


    private boolean initialized = false; //when not initalized only name, id and pending!

    public OtherUser(ClientApplication senderApp, ClientApplication recieverApp, String username, List<Message> messages, boolean pending, int id, Database database, boolean initialized) {
        this.id = id;
        this.senderApp = senderApp;
        this.recieverApp = recieverApp;
        this.username = username;
        this.messages = messages;
        this.pending = pending;
        this.database = database;
        this.initialized = initialized;
    }

    @Override
    public String toString() {
        if (pending) {
            return "Pending user";
        } else {
            return username;
        }
    }

    public static OtherUser createPendingUser(BulletinBoard board, Database db) throws NoSuchAlgorithmException, RemoteException, SQLException {
        ClientApplication reciever = ClientApplication.createReciever(board);
        OtherUser user = new OtherUser(null, reciever, null, new ArrayList<>(), true, -1, db, true);
        db.addRecieverUser(user);
        return user;
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
}