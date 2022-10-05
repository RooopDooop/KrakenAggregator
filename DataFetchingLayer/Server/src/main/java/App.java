import MSSQL.AssetTask.timerAssets;
import MSSQL.PairTask.timerPairs;
import MSSQL.SQLConn;
import WebsocketServer.websocketRouting;
import org.java_websocket.server.WebSocketServer;
import java.net.*;
import java.util.Timer;

public class App {
    public static void main(String[] args) {
        SQLConn conn = new SQLConn();

        Timer AssetTimer = new Timer();
        AssetTimer.scheduleAtFixedRate(new timerAssets(), 0, 100000);

        Timer PairPullTimer = new Timer();
        PairPullTimer.scheduleAtFixedRate(new timerPairs(), 0, 100000);

        String host = "localhost";
        int port = 8080;

        WebSocketServer server = new websocketRouting(new InetSocketAddress(host, port));
        server.run();
    }
}
