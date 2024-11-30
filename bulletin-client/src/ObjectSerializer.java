import java.io.*;

/**
 * bulletin-board: ObjectSerializer
 *
 * @author robbe
 * @version 30/11/2024
 */


public class ObjectSerializer {
    public static <T extends Serializable> byte[] serialize(T obj) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(byteOut)) {
            out.writeObject(obj);
            return byteOut.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Serializable> T deserialize(byte[] bytes) {
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
             ObjectInputStream in = new ObjectInputStream(byteIn)) {
            return (T) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}