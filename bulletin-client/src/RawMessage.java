import java.util.Arrays;
import java.util.Base64;

/**
 * bulletin-board: RawMessage
 *
 * @author robbe
 * @version 30/11/2024
 */

public class RawMessage {

    public final MessageType type;

    private final byte[] content;


    private RawMessage(MessageType type, byte[] content) {
        this.type = type;
        this.content = content;

    }

    public static RawMessage fromByteArray(byte[] bytes) {
        MessageType type = MessageType.fromByte(bytes[0]);
        if (type == null) {
           throw new RuntimeException("Invalid message type");
        }
        return new RawMessage(type, Arrays.copyOfRange(bytes, 1, bytes.length));
    }

    public byte[] toByteArray() {
        byte typeByte = type.getValue();
        byte[] finalByteArray = new byte[content.length + 1];
        finalByteArray[0] = typeByte;
        System.arraycopy(content, 0, finalByteArray, 1, content.length);
        return finalByteArray;
    }


    public static RawMessage fromKeyTransferDTO(KeyTransferDTO keyTransferDTO) {
        byte typeByte = MessageType.INIT.getValue();
        return new RawMessage(MessageType.fromByte(typeByte), ObjectSerializer.serialize(keyTransferDTO));
    }

    public KeyTransferDTO toKeyTransferDTO() {
        if (type != MessageType.INIT) {
            throw new RuntimeException("Only INIT messages can be converted to KeyTransferDTO");
        }
        return ObjectSerializer.deserialize(content);
    }


    public static RawMessage fromMessageDTO(MessageDTO messageDTO) {
        byte typeByte = MessageType.MESSAGE.getValue();
        return new RawMessage(MessageType.fromByte(typeByte), ObjectSerializer.serialize(messageDTO));
    }

    public MessageDTO toMessageDTO() {
        if (type != MessageType.MESSAGE) {
            throw new RuntimeException("Only MESSAGE messages can be converted to MessageDTO");
        }
        return ObjectSerializer.deserialize(content);
    }




    @Override
    public String toString() {
        return "RawMessage{" +
                "type=" + type +
                ", content=" + Arrays.toString(content) +
                '}';
    }
}