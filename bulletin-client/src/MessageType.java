/**
 * bulletin-board: BoardContent
 *
 * @author robbe
 * @version 16/11/2024
 */

//type enum
public enum MessageType {
    MESSAGE((byte) 0),
    INIT((byte) 1);

    private final byte value;

    MessageType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static MessageType fromByte(byte b) {
        for (MessageType type : MessageType.values()) {
            if (type.getValue() == b) {
                return type;
            }
        }
        return null;
    }
}
