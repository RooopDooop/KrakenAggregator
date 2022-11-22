package WebsocketServer;

import Mongo.MongoConn;
import com.google.gson.Gson;
import org.bson.types.ObjectId;
import org.java_websocket.WebSocket;

public class wsMessage {
    public static class WSJson {
        private final String MessageID;
        private final String Action;
        private final String Message;

        public WSJson(String MessageID, String Action, String Message) {
            this.MessageID = MessageID;
            this.Action = Action;
            this.Message = Message;
        }
    }

    /*private final ObjectId MessageID;
    private final String Action;
    private final String Message;*/
    private final ObjectId MessageID;
    private final String Action;
    private final String Message;
    private final WebSocket conn;

    public wsMessage(WebSocket conn, String Action, String Message) {
        System.out.println(Action);
        this.MessageID = MongoConn.WriteLog(Action, Message, conn);
        this.Action = Action;
        this.Message = Message;
        this.conn = conn;
    }

    public ObjectId returnID() {
        return this.MessageID;
    }
    public String returnAction() {
        return this.Action;
    }
    public String returnMessage() {
        return this.Message;
    }
    public WebSocket returnConn() {
        return this.conn;
    }
    public String returnJSON() {
        return new Gson().toJson(new WSJson(this.MessageID.toHexString(), this.Action, this.Message));
    }
}
