package in.sdtechnocrat.messenger;

public class MessageData {

    private String message;
    private String receiverID;
    private String senderID;
    private String time;

    public MessageData(String message, String receiverID, String senderID, String time) {
        this.message = message;
        this.receiverID = receiverID;
        this.senderID = senderID;
        this.time = time;
    }

    public MessageData() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
