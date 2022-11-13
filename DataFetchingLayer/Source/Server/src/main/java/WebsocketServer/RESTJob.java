package WebsocketServer;

import org.java_websocket.WebSocket;

public class RESTJob implements Comparable<RESTJob> {
    private WebSocket conn;
    private String Category;
    private String TargetURL;
    private int StarvationIndex;

    public RESTJob(WebSocket conn, String Category, String TargetURL) {
        this.conn = conn;
        this.Category = Category;
        this.TargetURL = TargetURL;
        this.StarvationIndex = 0;
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

    public int returnStarvation() { return this.StarvationIndex; }
    public void addStarvation(int starveAddition) { this.StarvationIndex = this.StarvationIndex + starveAddition; }
    public void removeStarvation() { this.StarvationIndex = 0; }

    @Override
    public int compareTo(RESTJob foreignJob) {
        if (StarvationIndex > foreignJob.returnStarvation()) {
            return -1;
        } else {
            return 1;
        }
    }
}
