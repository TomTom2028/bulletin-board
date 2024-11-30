import java.io.Serializable;

/**
* bulletin-board: MessageDTO
* @author robbe
* @version 30/11/2024
*/

public class MessageDTO implements Serializable {
    public String content;
    public String sendTime;

    public MessageDTO(String content, String sendTime) {
        this.content = content;
        this.sendTime = sendTime;
    }
}