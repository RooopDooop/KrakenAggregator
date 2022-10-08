package WebsocketServer;

import org.java_websocket.WebSocket;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class RESTQueue extends Thread {
    private final BlockingQueue<RESTJob> jobQueue = new LinkedBlockingDeque<>();
    private long latestRunTime = 0;

    @Override
    public void run() {
        while (true) {
            try {
                ProcessJob(jobQueue.take());
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void AddJob(wsMessage objMessage) throws Exception {
        RESTJob objJob = new RESTJob(objMessage.returnConn(), objMessage.returnAction(), objMessage.returnMessage());
        if (!jobQueue.offer(objJob)) {
            throw new Exception("QueueInsertFailed");
        }
    }

    public void ClearJobs() {
        System.out.println("Clearing jobs...");
        this.latestRunTime = 0;
        jobQueue.clear();
    }

    private void ProcessJob(RESTJob objJob) {

        System.out.println(System.currentTimeMillis() - latestRunTime + " - Processed: " + objJob.returnConn().getRemoteSocketAddress() + " - Category: " + objJob.returnCategory() + " - URL: " + objJob.returnTargetURL());

        this.latestRunTime = System.currentTimeMillis();
    }
}
