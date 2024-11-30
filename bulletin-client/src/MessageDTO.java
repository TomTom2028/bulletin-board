import java.io.Serializable;
import java.time.ZonedDateTime;

/**
* bulletin-board: MessageDTO
* @author robbe
* @version 30/11/2024
*/

public class MessageDTO implements Serializable {
    public String content;
    public ZonedDateTime sendTime;

    public MessageDTO(String content, ZonedDateTime sendTime) {
        this.content = content;
        this.sendTime = sendTime;
    }
}