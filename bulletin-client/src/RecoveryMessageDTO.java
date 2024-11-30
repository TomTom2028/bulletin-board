import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * bulletin-board: RecoveryMessageDTO
 *
 * @author robbe
 * @version 30/11/2024
 */

public class RecoveryMessageDTO implements Serializable {
    public String content;
    public ZonedDateTime sendTime;

    public boolean sendByMe;
    public int id;

    public RecoveryMessageDTO(String content, ZonedDateTime sendTime, boolean sendByMe, int id) {
        this.content = content;
        this.sendTime = sendTime;
        this.sendByMe = sendByMe;
        this.id = id;
    }
}