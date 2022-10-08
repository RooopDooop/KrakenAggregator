package WebsocketServer;

import MSSQL.SQLConn;
import org.java_websocket.WebSocket;
import org.javatuples.Octet;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class websocketQueue extends Thread {
    BlockingQueue<wsMessage> messageQueue = new LinkedBlockingDeque<>();
    private final HashMap<WebSocket, websocketClient> listClientConnections = new HashMap<>();
    RESTQueue objJobQueue = new RESTQueue();
    //private final RESTQueue objRESTQueue = new RESTQueue();

    @Override
    public void run() {
        objJobQueue.start();

        while (true) {
            try {
                ProcessMessage(messageQueue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void AddMessage(WebSocket conn, String Action, String Message) throws Exception {
        wsMessage objMessage = new wsMessage(conn, Action, Message);
        if (!messageQueue.offer(objMessage)) {
            throw new Exception("QueueInsertFailed");
        }
    }

    public void AddClient(WebSocket conn) {
        try {
            listClientConnections.put(conn, new WebsocketServer.websocketClient(conn));
            AddMessage(conn, "ClientWelcoming", "N/A");
            distributePairs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void RemoveClient(WebSocket conn) {
        try {
            listClientConnections.remove(conn);
            AddMessage(conn, "ClientDisconnected", "N/A");
            distributePairs();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void ProcessMessage(wsMessage objMessage) {
        if (objMessage.returnID() == 0) {
            objMessage.generateID(objMessage.returnConn());
        }

        switch (objMessage.returnAction()) {
            case "ClientWelcoming" -> {
                objMessage.returnConn().send(objMessage.returnJSON());
            }
            case "AssignPairs" -> {
                System.out.println("Allocating Pairs to: " + objMessage.returnConn().getRemoteSocketAddress());
                objMessage.returnConn().send(objMessage.returnJSON());
            }
            case "ClientDisconnected" -> {
                System.out.println("Client disconnected: " + objMessage.returnConn().getRemoteSocketAddress());
            }
            case "ScheduleTrade" -> {
                try {
                    this.objJobQueue.AddJob(objMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void distributePairs() {
        this.objJobQueue.ClearJobs();

        if (this.listClientConnections.size() > 0) {
            HashMap<Integer, String> clientAssignment = new HashMap<>();
            Map<String, Octet<String, Integer, Integer, Integer, Integer, Integer, Integer, BigDecimal>> mapTupleValues = new HashMap<>();
            try {
                mapTupleValues = new SQLConn().fetchPairs();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            //TODO re-write this using proper class
            int latestInput = 0;
            for (Octet<String, Integer, Integer, Integer, Integer, Integer, Integer, BigDecimal> tupleValues : mapTupleValues.values()) {
                if (clientAssignment.get(latestInput) == null) {
                    clientAssignment.put(latestInput, tupleValues.getValue0());
                } else {
                    clientAssignment.put(latestInput, clientAssignment.get(latestInput) + ", " + tupleValues.getValue0());
                }

                latestInput++;
                if (latestInput > (listClientConnections.size() - 1)) {
                    latestInput = 0;
                }
            }

            int clientIndex = 0;
            for (websocketClient objClient : listClientConnections.values()) {
                try {
                    AddMessage(objClient.clientConn, "AssignPairs", clientAssignment.get(clientIndex));
                    clientIndex++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("All clients have disconnected");
        }
    }
}
