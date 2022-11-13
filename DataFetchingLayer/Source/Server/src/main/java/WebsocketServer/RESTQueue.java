package WebsocketServer;

import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import java.util.concurrent.PriorityBlockingQueue;

public class RESTQueue extends Thread {
    private final PriorityBlockingQueue<RESTJob> jobQueue = new PriorityBlockingQueue<>();

    @Override
    public void run() {
        while (true) {
            try {
                RESTJob objJob = jobQueue.take();

                this.jobQueue.forEach(queuedJob -> {
                    if (queuedJob.returnConn() != objJob.returnConn()) {
                        queuedJob.addStarvation(1);
                    } else {
                        queuedJob.removeStarvation();
                    }
                });

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
        } catch (WebsocketNotConnectedException e) {
            System.out.println(objJob.returnConn().getRemoteSocketAddress() + " - has disconnected, ignoring work");
        }

        long diffTime = System.currentTimeMillis() - epochStartTime;
        if (diffTime < 100) {
            try {
                Thread.sleep(100 - diffTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
