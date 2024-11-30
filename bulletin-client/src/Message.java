import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * bulletin-board: Message
 *
 * @author robbe
 * @version 16/11/2024
 */

public class Message {
    public String content;
    public ZonedDateTime sendTime;

    public boolean sendByMe;
    public int id;

    public Message(String content, ZonedDateTime sendTime, boolean sendByMe, int id) {
        this.content = content;
        this.sendTime = sendTime;
        this.sendByMe = sendByMe;
        this.id = id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String toFormattedString(OtherUser user) {
        // send time to date - time
        ZoneId currentZone = ZoneId.systemDefault();
        LocalDateTime localSendTime = sendTime.withZoneSameInstant(currentZone).toLocalDateTime();

        String formattedDay = localSendTime.getDayOfMonth() < 10 ? "0" + localSendTime.getDayOfMonth() : "" + localSendTime.getDayOfMonth();
        String formattedMonth = localSendTime.getMonthValue() < 10 ? "0" + localSendTime.getMonthValue() : "" + localSendTime.getMonthValue();
        String formattedHour = localSendTime.getHour() < 10 ? "0" + localSendTime.getHour() : "" + localSendTime.getHour();
        String formattedMinute = localSendTime.getMinute() < 10 ? "0" + localSendTime.getMinute() : "" + localSendTime.getMinute();

        String localSendTimeStr = String.format("%s/%s/%s %s:%s", formattedDay, formattedMonth, localSendTime.getYear(), formattedHour, formattedMinute);

        String username = user.getUsername() == null ? "Unknown user" : user.getUsername();
        return ((sendByMe ? "You" : username)  + " (" + localSendTimeStr + "): " + content);
    }

    public MessageDTO toDTO() {
        return new MessageDTO(content, sendTime);
    }

    public static Message fromDTO(MessageDTO dto) {
        return new Message(dto.content, dto.sendTime, false, -1);
    }
}