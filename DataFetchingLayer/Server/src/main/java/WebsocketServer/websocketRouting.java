package WebsocketServer;

import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class websocketRouting extends WebSocketServer {
    websocketQueue objWSQueue = new websocketQueue();

    public websocketRouting(InetSocketAddress address) {
        super(address);
        objWSQueue.start();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        objWSQueue.AddClient(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        //Once a connection is lost, just redistribute pairs to the remaining clients.
        //This is faster and less complicated than checking all the clients to see if they still have their pairs
        System.out.println(reason);

        objWSQueue.RemoveClient(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        //TODO on message received, push to the queue where it will be handled
        try {
            wsMessage objReceived = new Gson().fromJson(message, wsMessage.class);
            objWSQueue.AddMessage(conn, objReceived.returnAction(), objReceived.returnMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        //TODO maybe write this to an MSSQL instance
        //Errors like this seem to be within the websocket server, not received from the client.
        //Maybe send out an error message to the clients and shut down the whole operation?
        System.out.println(ex.toString());
        //disconnectClient(conn);
    }

    @Override
    public void onStart() {
        System.out.println("Started websocket server");
    }
}
