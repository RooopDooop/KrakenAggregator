import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class App extends WebSocketServer {
    Timer validationTimer;
    listAssetPairs listPairs = new listAssetPairs();
    HashMap<Integer, wsMessage> messageWatchlist = new HashMap<Integer, wsMessage>();

    public App(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        wsMessage objMessage = new Gson().fromJson(message, wsMessage.class);

        switch (objMessage.returnAction()) {
            case "RequestPair": {
                String assetPair = listPairs.returnRandomPair();
                wsMessage objResponse = new wsMessage("AssignPair", assetPair);
                conn.send(new Gson().toJson(objResponse));
                break;
            }
            case "PairReceived": {
                System.out.println("Client: " + conn.getRemoteSocketAddress() + " has successfully gotten: " + objMessage.returnMessage() + ", message ID: " + objMessage.returnID());
                listPairs.assignPairClient(objMessage.returnMessage(), conn);
                break;
            }
            case "AssignProxy": {
                try {
                    //TODO have this called directly from the client to proxy fetcher

                    HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/randomProxy")).build();
                    HttpClient clientHTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
                    HttpResponse<String> response = clientHTTP.send(request, HttpResponse.BodyHandlers.ofString());
                    instanceProxy objProxy = new Gson().fromJson(response.body(), instanceProxy.class);

                    wsMessage objResponse = new wsMessage("ReceiveProxy", new Gson().toJson(objProxy.returnIPPort()));

                    conn.send(new Gson().toJson(objResponse));
                    System.out.println("Client: " + conn.getRemoteSocketAddress() + " has requested a proxy and was assigned: " + objProxy.returnIPPort());
                } catch (Exception eURL) {
                    throw new RuntimeException(eURL);
                }
                break;
            }
            case "unbindPair": {
                System.out.println("Client: " + conn.getRemoteSocketAddress() + " has unbound: " + objMessage.returnMessage());
                listPairs.unassignPairClient(objMessage.returnMessage());
                break;
            }
            default: {
                conn.send("messageParseFailure");
                break;
            }
        }
    }

    @Override
    public void onMessage( WebSocket conn, ByteBuffer message ) {
        System.out.println("received ByteBuffer from "	+ conn.getRemoteSocketAddress());
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("an error occurred on connection " + conn.getRemoteSocketAddress()  + ":" + ex);
    }
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        /*wsMessage objResponse = new wsMessage("WelcomeMessage", "Successfully connected!");
        conn.send(new Gson().toJson(objResponse));*/

        wsMessage objBroadcast = new wsMessage("ClientConnected", handshake.getResourceDescriptor());
        broadcast(new Gson().toJson(objBroadcast)); //This method sends a message to all clients connected

        System.out.println("new connection to " + conn.getRemoteSocketAddress());

        //TODO each connection should start a timer for each assetpair that checks its connection
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onStart() {
        System.out.println("server started successfully");
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8082;

        WebSocketServer server = new App(new InetSocketAddress(host, port));
        server.run();

    }

    /*class cycleValidity extends TimerTask {
        public void run() {
            System.out.println("Checking client validity");
            HashMap<String, listAssetPairs.AssetPair> assignedPairs = listPairs.returnAssignedPairs();

            for (listAssetPairs.AssetPair objAssetPair : assignedPairs.values()) {
                //TODO for each of these, have the AssetPair object query the client, if client returns a false. Then client no longer is using that asset and it is reset to "nobody"
                System.out.println("Checking client: " + objAssetPair.returnClient() + " - for: " +objAssetPair.returnPair());

                //TODO will need to send a message to the client... would need to have all the clients saved somehow, that would be a class in a hashset
                objAssetPair.sendVerifyClient(this.conn, objAssetPair.returnPair());

            }
        }
    }*/
}
