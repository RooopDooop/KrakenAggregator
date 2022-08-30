import org.java_websocket.WebSocket;

public class websocketClient {
    //WebsocketConnection
    //VerifyAssetPair (timer)
    //AssignedAssetPair
    //AssignPairClient
    //UnassignPairClient

    WebSocket clientConn;
    AssetPair assignedAssetPair;

    public websocketClient(WebSocket conn) {
        this.clientConn = conn;

        System.out.println("Connected: " + conn.getRemoteSocketAddress());
    }

    public void assignAssetPair(AssetPair assignedPair) {
        this.assignedAssetPair = assignedPair;
    }

    public AssetPair returnAssetPair() {
        return this.assignedAssetPair;
    }
}
