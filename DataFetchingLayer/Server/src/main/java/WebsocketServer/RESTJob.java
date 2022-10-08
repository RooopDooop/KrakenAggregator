package WebsocketServer;

import org.java_websocket.WebSocket;

public class RESTJob {
    private WebSocket conn;
    private String Category;
    private String TargetURL;

    public RESTJob(WebSocket conn, String Category, String TargetURL) {
        this.conn = conn;
        this.Category = Category;
        this.TargetURL = TargetURL;
    }

    public WebSocket returnConn() {
        return this.conn;
    }

    public String returnCategory() {
        return this.Category;
    }

    public String returnTargetURL() {
        return this.TargetURL;
    }
}
