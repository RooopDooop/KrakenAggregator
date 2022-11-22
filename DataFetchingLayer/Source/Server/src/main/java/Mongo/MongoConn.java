package Mongo;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.function.Consumer;

public class MongoConn {
    private static MongoClient mongoClient;
    private static MongoDatabase mongoDatabase;

    //MongoCollection mongoCollection = mongoDatabase.getCollection("Assets");

    public MongoConn() {
        getMongo();
    }

    public static MongoClient getMongo() {
        if (mongoClient == null) {
            mongoClient = new MongoClient(new MongoClientURI("mongodb://TowerDocker:27017"));
            mongoDatabase = mongoClient.getDatabase("KrakenDB");
        }

        return mongoClient;
    }

    public static ObjectId WriteLog(String Action, String Message, WebSocket connection) {
        Document objMessage = new Document("Action", Action).append("Message", Message).append("ServerConnection", connection.getLocalSocketAddress().toString()).append("ClientConnection", connection.getRemoteSocketAddress().toString());
        mongoDatabase.getCollection("wsLogs").insertOne(objMessage);

        System.out.println("Writing log: " + objMessage);

        ObjectId test = (ObjectId)objMessage.get("_id");

        System.out.println(test.toHexString());

        return test;
    }

    public static ArrayList<String> FindPairs() {
        Document objMessage = new Document("isFiat", false).append("Status", "enabled");
        ArrayList<String> arrAlternativePairs = new ArrayList<>();

        for (Object rawAsset : mongoDatabase.getCollection("Assets").find(objMessage)) {
            Document objAsset = (org.bson.Document)rawAsset;

            ArrayList<Document> arrPairs = (ArrayList<Document>)objAsset.get("Pairs");

            if (arrPairs != null) {
                for (var objPair : arrPairs) {
                    if (objPair.get("Status").equals("online")) {
                        arrAlternativePairs.add(objPair.get("AlternativePairName").toString());
                    }
                }
            }
        }

        return arrAlternativePairs;
    }
}
