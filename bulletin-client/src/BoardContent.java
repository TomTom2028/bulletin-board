

public class BoardContent {

    public MessageType type;
    public byte[] message;
    public int idx;
    public byte[] tag;

    public BoardContent(byte[] message, int idx, byte[] tag, MessageType type) {
        this.type = type;
        this.message = message;
        this.idx = idx;
        this.tag = tag;
    }


    public byte[] toByteArray() {
        byte[] idxBytes = intToByteArray(this.idx);
        byte typeByte = (byte) type.ordinal();
        byte[] bytes = new byte[1 + message.length + idxBytes.length + tag.length];

        bytes[0] = typeByte; // Add type byte
        System.arraycopy(message, 0, bytes, 1, message.length); // Start at index 1
        System.arraycopy(idxBytes, 0, bytes, 1 + message.length, idxBytes.length); // After message
        System.arraycopy(tag, 0, bytes, 1 + message.length + idxBytes.length, tag.length); // After idx
        return bytes;
    }


    public static BoardContent fromByteArray(byte[] bytes, int tagSize) {
        // Extract the type (first byte)
        byte typeByte = bytes[0];
        MessageType type = MessageType.values()[typeByte]; // Convert back to enum

        // Adjust indices to match serialized order
        int messageLength = bytes.length - 1 - Integer.BYTES - tagSize;
        byte[] messageBytes = new byte[messageLength];
        byte[] idxBytes = new byte[Integer.BYTES];
        byte[] tag = new byte[tagSize];

        System.arraycopy(bytes, 1, messageBytes, 0, messageLength); // Start after type
        System.arraycopy(bytes, 1 + messageLength, idxBytes, 0, Integer.BYTES); // After message
        System.arraycopy(bytes, 1 + messageLength + Integer.BYTES, tag, 0, tagSize); // After idx

        return new BoardContent(messageBytes, byteArrayToInt(idxBytes), tag, type);
    }


    public static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    public static int byteArrayToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8 ) |
                ((bytes[3] & 0xFF));

    }
}