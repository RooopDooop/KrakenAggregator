import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class websocketClient {
    //WebsocketConnection
    //VerifyAssetPair (timer)
    //AssignedAssetPair
    //AssignPairClient
    //UnassignPairClient

    WebSocket clientConn;
    AssetPair assignedAssetPair;
    TimerTask timerTask = new tickValidity();

    HashMap<Integer, wsMessage> listMessageHistory = new HashMap<>();

    public websocketClient(WebSocket conn) {
        this.clientConn = conn;

        System.out.println("Connected: " + conn.getRemoteSocketAddress());
    }

    public void assignAssetPair(AssetPair assignedPair) {
       //This function assigns the asset pair and begins the verification timer
        this.assignedAssetPair = assignedPair;

        new Timer().scheduleAtFixedRate(timerTask, 0, 5000);
    }

    public AssetPair returnAssetPair() {
        return this.assignedAssetPair;
    }

    public void sendMessage(wsMessage desiredMessage) {
        clientConn.send(desiredMessage.returnJSON());
        this.listMessageHistory.put(desiredMessage.returnID(), desiredMessage);
    }

    public HashMap<Integer, wsMessage> returnMessageHistory() {
        return this.listMessageHistory;
    }

    class tickValidity extends TimerTask {
        public void run() {
            System.out.println("Checking " + clientConn.getRemoteSocketAddress() + " validity: " + assignedAssetPair.returnPair());

            //TODO send validity message to client

            wsMessage objValidity = new wsMessage("tickVerification", assignedAssetPair.returnPair());
            sendMessage(objValidity);
        }
    }
    public void stopValidityCheck() {
        this.timerTask.cancel();
    }
}
