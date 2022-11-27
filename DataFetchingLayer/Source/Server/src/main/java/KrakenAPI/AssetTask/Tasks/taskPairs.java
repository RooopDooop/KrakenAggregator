package KrakenAPI.AssetTask.Tasks;
import KrakenAPI.AssetTask.Objects.krakenFee;
import KrakenAPI.AssetTask.Objects.krakenPair;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class taskPairs extends TimerTask {
    @Override
    public void run() {
        System.out.println("=========================Running Pair Task=========================");

        Map<String, krakenPair> krakenPairs = new HashMap<>();

        try {
            URL url = new URL("https://api.kraken.com/0/public/AssetPairs");

            HttpURLConnection HTTPConn = (HttpURLConnection) url.openConnection();
            HTTPConn.setRequestMethod("GET");
            HTTPConn.setRequestProperty("Accept", "application/json");

            if (HTTPConn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + HTTPConn.getResponseCode());
            }

            BufferedReader returnData = new BufferedReader(new InputStreamReader((HTTPConn.getInputStream())));

            //This while loop is miss-leading, it will only run once, look at the data structure for more information
            String output;
            while ((output = returnData.readLine()) != null) {
                Map<String, LinkedTreeMap<String, Object>> objKrakenData = new Gson().fromJson(new Gson().fromJson(output, JsonObject.class).get("result"), Map.class);

                for (LinkedTreeMap<String, Object> treeKraken : objKrakenData.values()) {
                    krakenPair objKrakenPair = new krakenPair(treeKraken);
                    krakenPairs.put(objKrakenPair.returnAlternativeName(), objKrakenPair);
                }

                for (krakenPair objPair : krakenPairs.values()) {
                    String[] websocketName = objPair.returnWebsocketName().split("/");

                    try {
                        objPair.WriteToMongo(websocketName[0]);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
