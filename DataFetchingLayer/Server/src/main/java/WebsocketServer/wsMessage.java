package WebsocketServer;

import MSSQL.SQLConn;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.java_websocket.WebSocket;
import org.javatuples.Triplet;

import javax.swing.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class wsMessage implements Comparable<wsMessage> {
    private class WSJson {
        private final int MessageID;
        private final String Action;
        private final String Message;

        public WSJson(int MessageID, String Action, String Message) {
            this.MessageID = MessageID;
            this.Action = Action;
            this.Message = Message;
        }
    }

    private int MessageID = 0;
    private final String Action;
    private final String Message;
    private final WebSocket conn;

    public wsMessage(WebSocket conn, String Action, String Message) {
        this.Action = Action;
        this.Message = Message;
        this.conn = conn;
        generateID(conn);
    }

    public void generateID(WebSocket conn) {
        /*try {
            this.MessageID = new SQLConn().insertWSMessage(Action, Message, conn.getLocalSocketAddress().toString(), conn.getRemoteSocketAddress().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    public int returnID() {
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
        return new Gson().toJson(new WSJson(this.MessageID, this.Action, this.Message));
    }

    @Override
    public int compareTo(wsMessage wsMessage) {
        return Integer.compare(this.returnID(), wsMessage.returnID());
    }
}
