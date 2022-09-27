package WebsocketServer;

import MSSQL.listPair.AssetPair;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class websocketClient {
    //WebsocketConnection
    //VerifyAssetPair (timer)
    //AssignedAssetPair
    //AssignPairClient
    //UnassignPairClient

    WebSocket clientConn;
    private TimerTask timerTask = new tickValidity();

    private List<AssetPair> AssignedPairs = new ArrayList<>();

    //AssetPair assignedAssetPair;
    //TimerTask timerTask = new tickValidity();

    //HashMap<Integer, WebsocketServer.wsMessage> listMessageHistory = new HashMap<>();

    public websocketClient(WebSocket conn) {
        this.clientConn = conn;
        new Timer().scheduleAtFixedRate(timerTask, 0, 5000);
    }

    public void sendMessage(wsMessage desiredMessage) {
        clientConn.send(desiredMessage.returnJSON());
        //this.listMessageHistory.put(desiredMessage.returnID(), desiredMessage);
    }

    public void assignPair(AssetPair inputPair) {
        this.AssignedPairs.add(inputPair);
    }

    //public HashMap<Integer, WebsocketServer.wsMessage> returnMessageHistory() {
       // return this.listMessageHistory;
    //}

    private class tickValidity extends TimerTask {
        public void run() {
            //WebsocketServer.wsMessage objValidity = new WebsocketServer.wsMessage("tickVerification", "");
            //sendMessage(objValidity);
        }
    }

    public void stopValidityCheck() {
        System.out.println("Stopping validity check");
        this.timerTask.cancel();
    }
}
