package WebsocketServer;

import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class RESTQueue extends Thread {
    private final BlockingQueue<RESTJob> jobQueue = new LinkedBlockingDeque<>();
    private long latestRunTime = 0;

    @Override
    public void run() {
        while (true) {
            try {
                RESTJob objJob = jobQueue.take();

                System.out.println("Running job: " + objJob.returnTargetURL() + " - Jobs left: " + this.jobQueue.size());
                ProcessJob(objJob, System.currentTimeMillis());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void AddJob(WebSocket wsConn, String Action, String Message) throws Exception {
        RESTJob objJob = new RESTJob(wsConn, Action, Message);
        if (!jobQueue.offer(objJob)) {
            throw new Exception("QueueInsertFailed");
        }
    }

    public void ClearJobs() {
        System.out.println("Clearing jobs...");
        this.latestRunTime = 0;
        jobQueue.clear();
    }

    private void ProcessJob(RESTJob objJob, long epochStartTime) {
        String processType = "";

        switch (objJob.returnCategory()) {
            case "ScheduleTrade" -> processType = "ProcessTrade";
            case "ScheduleTicker" -> processType = "ProcessTicker";
            case "ScheduleOHLC" -> processType = "ProcessOHLC";
            case "ScheduleOrder" -> processType = "ProcessOrder";
            default -> System.out.println("Unknown job type: " + objJob.returnCategory());
        }

        wsMessage objMessage = new wsMessage(objJob.returnConn(), processType, objJob.returnTargetURL());
        try {
            objJob.returnConn().send(objMessage.returnJSON());
            this.latestRunTime = System.currentTimeMillis();
        } catch (WebsocketNotConnectedException e) {
            System.out.println(objJob.returnConn().getRemoteSocketAddress() + " - has disconnected, ignoring work");
        }

        long diffTime = System.currentTimeMillis() - epochStartTime;
        if (diffTime < 125) {
            try {
                Thread.sleep(125 - diffTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
