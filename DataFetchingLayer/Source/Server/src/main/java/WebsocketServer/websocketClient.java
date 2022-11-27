package WebsocketServer;

import org.java_websocket.WebSocket;
import java.util.TimerTask;

public class websocketClient {
    WebSocket clientConn;

    public websocketClient(WebSocket conn) {
        this.clientConn = conn;
    }

    public void sendMessage(wsMessage desiredMessage) {
        clientConn.send(desiredMessage.returnJSON());
    }
}
