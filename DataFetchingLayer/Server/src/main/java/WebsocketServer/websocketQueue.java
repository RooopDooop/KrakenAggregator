package WebsocketServer;

import MSSQL.listPair.AssetPair;
import MSSQL.listPair.listPair;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class websocketQueue extends Thread {
    HashMap<WebSocket, websocketClient> listClientConnections = new HashMap<>();
    private final BlockingQueue<wsMessage> messageQueue = new LinkedBlockingDeque<>();

    @Override
    public void run() {
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
        listClientConnections.remove(conn);
        distributePairs();
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
        }
    }

    private void distributePairs() {
        if (this.listClientConnections.size() > 0) {
            HashMap<Integer, String> clientAssignment = new HashMap<>();
            Object[] hashArray = new listPair().getSQLPairs().values().toArray();

            int latestInput = 0;
            for (Object objPair : hashArray) {
                if (clientAssignment.get(latestInput) == null) {
                    clientAssignment.put(latestInput, ((AssetPair) objPair).getAlternativeName());
                } else {
                    clientAssignment.put(latestInput, clientAssignment.get(latestInput) + ", " + ((AssetPair) objPair).getAlternativeName());
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
        }
    }
}
