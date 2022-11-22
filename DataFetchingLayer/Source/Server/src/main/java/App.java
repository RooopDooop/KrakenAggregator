import KrakenAPI.AssetTask.Tasks.taskAssets;
import KrakenAPI.AssetTask.Tasks.taskPairs;
import WebsocketServer.websocketRouting;
import Mongo.MongoConn;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main(String[] args) {
        MongoConn mongoConn = new MongoConn();

        //SQLConn conn = new SQLConn();

        //Timer ConversionsTimer = new Timer();
        //ConversionsTimer.scheduleAtFixedRate(new timerConversions(), 0, TimeUnit.SECONDS.toMillis(10));

        Timer AssetTimer = new Timer();
        AssetTimer.scheduleAtFixedRate(new taskAssets(), 0, TimeUnit.HOURS.toMillis(12));

        Timer PairPullTimer = new Timer();
        PairPullTimer.scheduleAtFixedRate(new taskPairs(), 0, TimeUnit.HOURS.toMillis(12));

        String host = "0.0.0.0";
        int port = 8081;

        WebSocketServer server = new websocketRouting(new InetSocketAddress(host, port));
        server.setConnectionLostTimeout(200);
        server.run();
    }
}
