import MSSQL.PairTask.timerPairs;
import MSSQL.SQLConn;
import MSSQL.listAsset.listAsset;
import MSSQL.listPair.listPair;
import WebsocketServer.websocketRouting;
import org.java_websocket.server.WebSocketServer;

import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

public class App {

    public static void main(String[] args) {
        SQLConn conn = new SQLConn();

        //new listAsset();
        //new listPair();

        Timer AssetPullTimer = new Timer();
        AssetPullTimer.scheduleAtFixedRate(new timerPairs(), 0, 100000);

        //Timer AssetPullTimer = new Timer();
        //AssetPullTimer.scheduleAtFixedRate(new AssetPull(), 0, 60000);

        String host = "localhost";
        int port = 8080;

        WebSocketServer server = new websocketRouting(new InetSocketAddress(host, port));
        server.run();
    }
}
