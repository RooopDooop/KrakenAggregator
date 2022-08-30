import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import redis.clients.jedis.Jedis;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class websocketRouting extends WebSocketServer {
    HashMap<WebSocket, websocketClient> listClientConnections = new HashMap<>();
    Set<AssetPair> listAssetPairs = new HashSet<>();

    public websocketRouting(InetSocketAddress address) {
        super(address);

        listAssetPairs.clear();
        Jedis jedis = new Jedis();
        Set<String> returnKeys = jedis.keys("*AssetPair*");
        returnKeys.forEach((String strJedis) -> {
            listAssetPairs.add(new AssetPair(strJedis));
        });
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        listClientConnections.put(conn, new websocketClient(conn));

        //TODO use the GSON library for json conversion and wsMessage object
        wsMessage objBroadcast = new wsMessage("ClientConnected", conn.getRemoteSocketAddress().toString());
        broadcast(new Gson().toJson(objBroadcast)); //This method sends a message to all clients connected

        System.out.println(this.findNotAssignedPairs());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Closing: " + conn.getRemoteSocketAddress());
        listClientConnections.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("Error: " + conn.getRemoteSocketAddress());
        listClientConnections.remove(conn);
    }

    @Override
    public void onStart() {
        System.out.println("Starting websocket server");
    }

    private Set <AssetPair> findAssignedPairs() {
        Set <AssetPair> listAssigned = new HashSet<>();

        for (websocketClient objClient : listClientConnections.values()) {
            AssetPair objAssigned = objClient.returnAssetPair();
            if (objAssigned != null) {
                System.out.println("Added: " + objAssigned);
                listAssigned.add(objAssigned);
            }
        }

        return listAssigned;
    }

    private Set <AssetPair> findNotAssignedPairs() {
        Set <AssetPair> listNotAssigned = listAssetPairs;

        for (websocketClient objClient : listClientConnections.values()) {
            AssetPair objAssigned = objClient.returnAssetPair();
            if (objAssigned != null) {
                System.out.println("Removed: " + objAssigned);
                listNotAssigned.remove(objAssigned);
            }
        }

        return listNotAssigned;
    }
}
