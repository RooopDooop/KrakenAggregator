import MSSQL.AssetTask.timerAssets;
import MSSQL.ConversionTask.timerConversions;
import MSSQL.PairTask.timerPairs;
import MSSQL.SQLConn;
import WebsocketServer.websocketRouting;
import org.java_websocket.server.WebSocketServer;
import java.net.*;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main(String[] args) {
        SQLConn conn = new SQLConn();
        System.out.println(TimeUnit.HOURS.toMillis(12));

        Timer ConversionsTimer = new Timer();
        ConversionsTimer.scheduleAtFixedRate(new timerConversions(), 0, TimeUnit.SECONDS.toMillis(10));

        Timer AssetTimer = new Timer();
        AssetTimer.scheduleAtFixedRate(new timerAssets(), 0, TimeUnit.HOURS.toMillis(12));

        Timer PairPullTimer = new Timer();
        PairPullTimer.scheduleAtFixedRate(new timerPairs(), 0, TimeUnit.HOURS.toMillis(12));

        String host = "localhost";
        int port = 8080;

        WebSocketServer server = new websocketRouting(new InetSocketAddress(host, port));
        server.run();
    }
}
