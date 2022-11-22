package KrakenAPI.AssetTask.Tasks;

import KrakenAPI.AssetTask.Objects.krakenAsset;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class taskAssets extends TimerTask {
    @Override
    public void run() {
        System.out.println("=========================Running Asset Task=========================");
        try {
            HttpURLConnection HTTPConn = (HttpURLConnection) new URL("https://api.kraken.com/0/public/Assets").openConnection();
            HTTPConn.setRequestMethod("GET");
            HTTPConn.setRequestProperty("Accept", "application/json");

            if (HTTPConn.getResponseCode() != 200) { throw new RuntimeException("Failed : HTTP error code : " + HTTPConn.getResponseCode());}

            BufferedReader returnData = new BufferedReader(new InputStreamReader((HTTPConn.getInputStream())));
            String output;
            while ((output = returnData.readLine()) != null) {
                for (Object JSONAsset : ((Map<String, Object>) new Gson().fromJson(output, HashMap.class).get("result")).values()) {
                    HashMap<String, Object> JSONValues = new Gson().fromJson(new Gson().toJson(JSONAsset), HashMap.class);

                    krakenAsset objAsset;
                    try {
                        objAsset = new krakenAsset(
                                JSONValues.get("altname").toString(),
                                JSONValues.get("aclass").toString(),
                                (int) Double.parseDouble(JSONValues.get("decimals").toString()),
                                (int) Double.parseDouble(JSONValues.get("display_decimals").toString()),
                                new BigDecimal(JSONValues.get("collateral_value").toString()),
                                JSONValues.get("status").toString()
                        );

                        objAsset.WriteToMongo();
                    } catch (NullPointerException e) {
                        objAsset = new krakenAsset(
                                JSONValues.get("altname").toString(),
                                JSONValues.get("aclass").toString(),
                                (int) Double.parseDouble(JSONValues.get("decimals").toString()),
                                (int) Double.parseDouble(JSONValues.get("display_decimals").toString()),
                                null,
                                JSONValues.get("status").toString()
                        );

                        objAsset.WriteToMongo();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
