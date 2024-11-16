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
    private String uuid;



    public OtherUser(ClientApplication senderApp, ClientApplication recieverApp, String username, List<Message> messages, String uuid) {
        this.senderApp = senderApp;
        this.recieverApp = recieverApp;
        this.username = username;
        this.messages = messages;
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return username;
    }
}