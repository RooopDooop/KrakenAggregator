import org.java_websocket.server.WebSocketServer;
import java.net.*;

public class App {

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8081;

        WebSocketServer server = new websocketRouting(new InetSocketAddress(host, port));
        server.run();
    }
}
