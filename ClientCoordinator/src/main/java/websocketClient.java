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
    private TimerTask timerTask = new tickValidity();

    //AssetPair assignedAssetPair;
    //TimerTask timerTask = new tickValidity();

    //HashMap<Integer, wsMessage> listMessageHistory = new HashMap<>();

    public websocketClient(WebSocket conn) {
        this.clientConn = conn;
        new Timer().scheduleAtFixedRate(timerTask, 0, 5000);
    }

    public void sendMessage(wsMessage desiredMessage) {
        clientConn.send(desiredMessage.returnJSON());
        //this.listMessageHistory.put(desiredMessage.returnID(), desiredMessage);
    }

    //public HashMap<Integer, wsMessage> returnMessageHistory() {
       // return this.listMessageHistory;
    //}

    class tickValidity extends TimerTask {
        public void run() {
            wsMessage objValidity = new wsMessage("tickVerification", "");
            sendMessage(objValidity);
        }
    }

    public void stopValidityCheck() {
        System.out.println("Stopping validity check");
        this.timerTask.cancel();
    }
}
