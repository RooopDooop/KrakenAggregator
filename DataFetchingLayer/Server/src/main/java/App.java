import KrakenAPI.AssetTask.Tasks.taskAssets;
import KrakenAPI.AssetTask.Tasks.taskPairs;
import MSSQL.SQLConn;
import WebsocketServer.websocketRouting;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main(String[] args) {
        SQLConn conn = new SQLConn();

        //Timer ConversionsTimer = new Timer();
        //ConversionsTimer.scheduleAtFixedRate(new timerConversions(), 0, TimeUnit.SECONDS.toMillis(10));

        Timer AssetTimer = new Timer();
        AssetTimer.scheduleAtFixedRate(new taskAssets(), 0, TimeUnit.HOURS.toMillis(12));

        Timer PairPullTimer = new Timer();
        PairPullTimer.scheduleAtFixedRate(new taskPairs(), 0, TimeUnit.HOURS.toMillis(12));

        String host = "localhost";
        int port = 8081;

        WebSocketServer server = new websocketRouting(new InetSocketAddress(host, port));
        server.run();
    }
}
