/**
 * bulletin-board: Message
 *
 * @author robbe
 * @version 16/11/2024
 */

public class Message {
    public String content;
    public String sendTime;

    public boolean sendByMe;

    public Message(String content, String sendTime, boolean sendByMe) {
        this.content = content;
        this.sendTime = sendTime;
        this.sendByMe = sendByMe;
    }

    public String toFormattedString(OtherUser user) {
        return (sendByMe ? "You" : user.getUsername()) + " (" + sendTime + "): " + content;
    }
}