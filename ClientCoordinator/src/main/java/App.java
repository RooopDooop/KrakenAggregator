import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class App extends WebSocketServer {
    public App(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send("Welcome to the server!"); //This method sends a message to the new client
        broadcast( "new connection: " + handshake.getResourceDescriptor() ); //This method sends a message to all clients connected
        System.out.println("new connection to " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("received message from "	+ conn.getRemoteSocketAddress() + ": " + message);
        wsMessage objMessage = new Gson().fromJson(message, wsMessage.class);

        System.out.println(objMessage.returnAction());
        System.out.println(objMessage.returnTimeSent());
        System.out.println(objMessage.returnMessage());

        switch (objMessage.returnAction()) {
            case "RequestPairs": {
                wsMessage objResponse = new wsMessage("AssigningPairs", "['USDBTX, 'CADJPY']");
                conn.send(new Gson().toJson(objResponse));
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
        int port = 8080;

        WebSocketServer server = new App(new InetSocketAddress(host, port));
        server.run();
    }
}
