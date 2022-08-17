public class wsMessage {
    private final String Action;
    private final int TimeSent;
    private final String Message;

    public wsMessage(String Action, String Message) {
        this.Action = Action;
        this.TimeSent = 69;
        this.Message = Message;
    }

    public String returnAction() {
        return this.Action;
    }
    public int returnTimeSent() {
        return this.TimeSent;
    }
    public String returnMessage() {
        return this.Message;
    }
}
