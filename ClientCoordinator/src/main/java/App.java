import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import redis.clients.jedis.Jedis;

import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class App extends WebSocketServer {
    listAssetPairs listPairs = new listAssetPairs();

    public App(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        wsMessage objResponse = new wsMessage("WelcomeMessage", "Successfully connected!");
        conn.send(new Gson().toJson(objResponse));

        wsMessage objBroadcast = new wsMessage("ClientConnected", handshake.getResourceDescriptor());
        broadcast( new Gson().toJson(objBroadcast)); //This method sends a message to all clients connected

        System.out.println("new connection to " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        wsMessage objMessage = new Gson().fromJson(message, wsMessage.class);

        switch (objMessage.returnAction()) {
            case "RequestPair": {
                String assetPair = listPairs.returnRandomPair();
                wsMessage objResponse = new wsMessage("AssignPair", assetPair);
                listPairs.assignPairClient(assetPair, conn.getRemoteSocketAddress().toString());
                conn.send(new Gson().toJson(objResponse));
                break;
            }
            case "PairReceived": {
                System.out.println("Client: " + conn.getRemoteSocketAddress() + " has successfully gotten its pairs, ID: " + objMessage.returnID());
                break;
            }
            case "AssignProxy": {
                try {
                    HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/randomProxy")).build();
                    HttpClient clientHTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
                    HttpResponse<String> response = clientHTTP.send(request, HttpResponse.BodyHandlers.ofString());
                    instanceProxy objProxy = new Gson().fromJson(response.body(), instanceProxy.class);

                    wsMessage objResponse = new wsMessage("ReceiveProxy", new Gson().toJson(objProxy));

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
    public void onStart() {
        System.out.println("server started successfully");
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8082;

        WebSocketServer server = new App(new InetSocketAddress(host, port));
        server.run();
    }
}
