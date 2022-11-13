package WebsocketServer;

import MSSQL.Objects.sqlPair;
import MSSQL.SQLConn;
import org.java_websocket.WebSocket;
import org.javatuples.Octet;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class websocketQueue extends Thread {
    final BlockingQueue<wsMessage> messageQueue = new LinkedBlockingDeque<>();
    private final HashMap<WebSocket, websocketClient> listClientConnections = new HashMap<>();
    private final RESTQueue objJobQueue = new RESTQueue();

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
                    case "ScheduleTrade", "ScheduleOrder", "ScheduleTicker", "ScheduleOHLC" -> {
                        String targetURL = "";

                        if (Objects.equals(objMessage.returnAction(), "ScheduleTrade")) {
                            targetURL = "https://api.kraken.com/0/public/Trades?pair=";
                        } else if (Objects.equals(objMessage.returnAction(), "ScheduleOrder")) {
                            targetURL = "https://api.kraken.com/0/public/Depth?pair=";
                        } else if (Objects.equals(objMessage.returnAction(), "ScheduleTicker")) {
                            targetURL = "https://api.kraken.com/0/public/Ticker?pair=";
                        } else if (Objects.equals(objMessage.returnAction(), "ScheduleOHLC")) {
                            targetURL = "https://api.kraken.com/0/public/OHLC?pair=";
                        }

                        try {
                            for (String strPair : objMessage.returnMessage().split(", ")) {
                                objJobQueue.AddJob(objMessage.returnConn(), objMessage.returnAction(), targetURL + strPair);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

        };
    }

    private void distributePairs() {
        this.objJobQueue.ClearJobs();

        if (this.listClientConnections.size() > 0) {
            HashMap<Integer, String> clientAssignment = new HashMap<>();
            Map<Integer, sqlPair> mapSQLPairs = new HashMap<>();

            try {
                mapSQLPairs = new SQLConn().getAllPairs();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            int latestInput = 0;
            for (sqlPair objPair : mapSQLPairs.values()) {
                if (clientAssignment.get(latestInput) == null) {
                    clientAssignment.put(latestInput, objPair.GetAlternativeName());
                } else {
                    clientAssignment.put(latestInput, clientAssignment.get(latestInput) + ", " + objPair.GetAlternativeName());
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
            this.objJobQueue.ClearJobs();
            System.out.println("All clients have disconnected");
        }
    }

    public int returnClientCount() {
        return this.listClientConnections.size();
    }
}
