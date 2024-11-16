/**
 * bulletin-board: BoardContent
 *
 * @author robbe
 * @version 16/11/2024
 */

public class BoardContent {
    public String message;
    public int idx;
    public byte[] tag;

    public BoardContent(String message, int idx, byte[] tag) {
        this.message = message;
        this.idx = idx;
        this.tag = tag;
    }


    public byte[] toByteArray() {
        byte[] messageBytes = message.getBytes();
        byte[] idxBytes = intToByteArray(this.idx);
        byte[] bytes = new byte[messageBytes.length + idxBytes.length + tag.length];
        System.arraycopy(messageBytes, 0, bytes, 0, messageBytes.length);
        System.arraycopy(idxBytes, 0, bytes, messageBytes.length, idxBytes.length);
        System.arraycopy(tag, 0, bytes, messageBytes.length + idxBytes.length, tag.length);
        return bytes;
    }

    public static BoardContent fromByteArray(byte[] bytes, int tagSize) {
        byte[] messageBytes = new byte[bytes.length - Integer.BYTES - tagSize];
        byte[] idxBytes = new byte[Integer.BYTES];
        byte[] tag = new byte[tagSize];
        System.arraycopy(bytes, 0, messageBytes, 0, messageBytes.length);
        System.arraycopy(bytes, messageBytes.length, idxBytes, 0, Integer.BYTES);
        System.arraycopy(bytes, messageBytes.length + Integer.BYTES, tag, 0, tagSize);
        return new BoardContent(new String(messageBytes), byteArrayToInt(idxBytes), tag);
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