import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

public class wsMessage {
    private final int MessageID;
    private final String Action;
    private final long TimeSent;
    private final String Message;

    public wsMessage(String Action, String Message) {
        this.Action = Action;
        this.TimeSent = Instant.now().toEpochMilli();
        this.Message = Message;
        this.MessageID = generateID();
    }

    private int generateID() {
        return ThreadLocalRandom.current().nextInt(1, 9999999);
    }

    public int returnID() { return this.MessageID; }
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
