import java.time.Instant;

public class wsMessage {
    private final String Action;
    private final long TimeSent;
    private final String Message;

    public wsMessage(String Action, String Message) {
        this.Action = Action;
        this.TimeSent = Instant.now().toEpochMilli();
        this.Message = Message;
    }

    public String returnAction() {
        return this.Action;
    }
    public long returnTimeSent() {
        return this.TimeSent;
    }
    public String returnMessage() {
        return this.Message;
    }
}
