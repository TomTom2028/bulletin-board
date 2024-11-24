/**
 * bulletin-board: ReceiveData
 *
 * @author robbe
 * @version 24/11/2024
 */

public class ReceiveData {
   String message;
   MessageType type;

    public ReceiveData(String message, MessageType type) {
         this.message = message;
         this.type = type;
    }
}