import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import redis.clients.jedis.Jedis;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class websocketRouting extends WebSocketServer {
    //HashMap<WebSocket, websocketClient> listClientConnections = new HashMap<>();
    HashMap<String, AssetPair> listAssetPairs = new HashMap<>();
    Jedis jedis = new Jedis("192.168.0.20", 6379);

    public websocketRouting(InetSocketAddress address) {
        super(address);

        listAssetPairs.clear();
        Set<String> returnKeys = jedis.keys("*AssetPair*");
        returnKeys.forEach((String strJedis) -> {
            AssetPair objPair = new AssetPair(jedis, strJedis);
            listAssetPairs.put(objPair.returnPair(), objPair);
        });
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        wsMessage objWelcome = new wsMessage("ClientWelcoming", "Welcome to the server!");
        //wsMessage objBroadcast = new wsMessage("ClientConnected", conn.getRemoteSocketAddress().toString());

        conn.send(objWelcome.returnJSON());

        //listClientConnections.put(conn, new websocketClient(conn));
        //listClientConnections.get(conn).sendMessage(objWelcome);

        //broadcastToOthers(objBroadcast, conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        //TODO iterate through all the pairs, check the connections

        for (AssetPair asset : listAssetPairs.values()) {
            if (asset.returnClient() != null) {
                if (asset.returnClient().clientConn == conn) {
                    System.out.println("Stopping client: " + asset.returnClient().clientConn.getRemoteSocketAddress());
                    asset.returnClient().stopValidityCheck();
                }
            }
        }

        System.out.println("Closing: " + conn.getRemoteSocketAddress());
        disconnectClient(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        wsMessage objReceived = new Gson().fromJson(message, wsMessage.class);

        switch (objReceived.returnAction()) {
            case "BeginPairWork": {
                //TODO assign pair client
                this.listAssetPairs.get(objReceived.returnMessage()).associateClient(conn);
            }
            case "tickVerifyResponse": {
                //This function is receiving the verification from the client that they are still watching their assigned pair
                //if (objReceived.returnMessage().equals("false")) {
                    //if validity check returns false, kill connection, in docker that would lead to a program stop and the whole container would shut down
                    //Because the connection is closed, the AssetPair variable populated in the connection will be deleted too, freeing up the AssetPair from the global list to be re-assigned.
                //    System.out.println("False verification: " + conn.getRemoteSocketAddress() + " for " + this.listClientConnections.get(conn).returnAssetPair().returnPair() +" Terminating.");
                 //   disconnectClient(conn);
                //}

                //TODO if pair isn't the same, then shutdown client
                if (this.listAssetPairs.get(objReceived.returnMessage()).returnClient().clientConn != conn) {
                    System.out.println("Verification failed: " + this.listAssetPairs.get(objReceived.returnMessage()).returnClient().clientConn.getRemoteSocketAddress());
                    this.listAssetPairs.get(objReceived.returnMessage()).stopClient();
                    disconnectClient(conn);
                }
            }
            /*case "RequestPair": {
                //This function is called by the client looking for a pair after connecting
                AssetPair[] arrPairs = this.findNotAssignedPairs().values().toArray(new AssetPair[0]);
                AssetPair objPair = arrPairs[new Random().nextInt(arrPairs.length - 1)];

                wsMessage objPairAssignment = new wsMessage("PairAssignment", objPair.returnPair());
                listClientConnections.get(conn).sendMessage(objPairAssignment);
                break;
            }
            case "PairReceived": {
                //This function is receiving the confirmation by the client that they are watching this assetPair, will also start the verify ticker
                listClientConnections.get(conn).assignAssetPair(this.listAssetPairs.get(objReceived.returnMessage()));
            }
           */
            default: {
                wsMessage objError = new wsMessage("ClientError", "messageParseFailure");
                break;
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        //TODO maybe write this to an MSSQL instance
        //Errors like this seem to be within the websocket server, not servers from the clients
        System.out.println(ex.toString());
        //disconnectClient(conn);
    }

    @Override
    public void onStart() {
        System.out.println("Started websocket server");

        spinUpClientsThread threadClients = new spinUpClientsThread();
        threadClients.start();

    }

    private void disconnectClient(WebSocket conn) {
        wsMessage objBroadcast = new wsMessage("ClientDisconnected", conn.getRemoteSocketAddress().toString());
        broadcastToOthers(objBroadcast, conn);

        //listClientConnections.get(conn).stopValidityCheck();
        //listClientConnections.remove(conn);
    }

    private void broadcastToOthers(wsMessage objBroadcast, WebSocket avoidConn) {
       /* for (websocketClient objClient : listClientConnections.values()) {
            if (objClient.clientConn != listClientConnections.get(avoidConn).clientConn) {
                objClient.sendMessage(objBroadcast);
            }
        }*/
    }

    private HashMap<String, AssetPair> findAssignedPairs() {
        HashMap<String, AssetPair>listAssigned = new HashMap<>();

        //for (websocketClient objClient : listClientConnections.values()) {
            //AssetPair objAssigned = objClient.returnAssetPair();
            /*if (objAssigned != null) {
                System.out.println("Added: " + objAssigned);
                listAssigned.put(objAssigned.returnPair(), objAssigned);
            }*/
        //}

        return listAssigned;
    }

    private HashMap<String, AssetPair> findNotAssignedPairs() {
        HashMap<String, AssetPair> listNotAssigned = listAssetPairs;

        //for (websocketClient objClient : listClientConnections.values()) {
            //AssetPair objAssigned = objClient.returnAssetPair();
            /*if (objAssigned != null) {
                System.out.println("Removed: " + objAssigned);
                listNotAssigned.remove(objAssigned.returnPair());
            }*/
        //}

        return listNotAssigned;
    }

    class spinUpClientsThread extends Thread {
        @Override
        public void run() {
            Set<StringBuilder> setClients = new HashSet<StringBuilder>();
            StringBuilder currentClient = new StringBuilder();

            for (AssetPair asset : listAssetPairs.values()) {
                if (asset.returnClient() == null) {
                    if (currentClient.toString().split(",").length < 5) {
                        if (currentClient.toString().equals("")) {
                            currentClient = new StringBuilder(asset.returnPair());
                        } else {
                            currentClient.append(",").append(asset.returnPair());
                        }
                    } else {
                        setClients.add(currentClient);
                        currentClient = new StringBuilder();
                    }

                        /*try {
                            asset.generateClient();
                            System.out.println("Spinning up docker client: " + asset.returnPair());

                            TimeUnit.MILLISECONDS.sleep(750);
                        } catch(InterruptedException ex) {
                            System.out.println(ex);
                        }*/
                }
            }

            for (StringBuilder strClient : setClients) {
                //asset.generateClient();
                System.out.println("Spinning up docker client: " + strClient);

                try {
                    Process process = Runtime.getRuntime().exec("docker run --net krakenNetwork -e PAIR_ENV=" + strClient.toString() + " watchmejump/krakenpredictor/assetclient:0.0.3");
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
        }
    }
}
