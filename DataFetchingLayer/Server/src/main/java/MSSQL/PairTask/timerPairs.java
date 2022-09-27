package MSSQL.PairTask;
import MSSQL.SQLConn;
import MSSQL.listPair.AssetPair;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LinkedTreeMap;
import org.javatuples.Octet;
import org.javatuples.Septet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class timerPairs extends TimerTask {
    @Override
    public void run() {
        System.out.println("Running pair timer task");

        Map<String, Octet<String, Integer, Integer, Integer, Integer, Integer, Integer, BigDecimal>> sqlValues = new HashMap<>();
        Map<String, Integer> sqlAssets = new HashMap<>();

        StringBuilder JSONInsert = new StringBuilder();
        StringBuilder JSONUpdate = new StringBuilder();

        try {
            sqlValues = new SQLConn().fetchPairs();
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            sqlAssets = new SQLConn().fetchAllAssets();
        } catch(Exception e) {
            e.printStackTrace();
        }

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
                JsonObject arrKrakenData = new Gson().fromJson(output, JsonObject.class);
                Map<String, Map> objKrakenData = new Gson().fromJson(arrKrakenData.get("result"), Map.class);

                for (Map mapPairs : objKrakenData.values()) {
                    if (sqlValues.get(mapPairs.get("altname")) == null) {
                        String[] wsNames = mapPairs.get("wsname").toString().split("/");
                        System.out.println(mapPairs.get("altname") + " - BaseID: " + sqlAssets.get(wsNames[0]) + ", QuoteID: " + sqlAssets.get(wsNames[1]) + " - Is missing from SQL, Inserting");
                    } else {
                        Octet<String, Integer, Integer, Integer, Integer, Integer, Integer, BigDecimal> objSQL = sqlValues.get(mapPairs.get("altname"));

                        int PairDecimals = objSQL.getValue2();
                        int LotDecimals = objSQL.getValue3();
                        int LotMultiplier = objSQL.getValue4();
                        int MarginCall = objSQL.getValue5();
                        int MarginStop = objSQL.getValue6();
                        BigDecimal OrderMinimum = new BigDecimal(objSQL.getValue7().toString()).stripTrailingZeros();

                        if (PairDecimals != (double)mapPairs.get("pair_decimals") || LotDecimals != (double)mapPairs.get("lot_decimals") || LotMultiplier != (double)mapPairs.get("lot_multiplier") || MarginCall != (double)mapPairs.get("margin_call") || MarginStop != (double)mapPairs.get("margin_stop") || !OrderMinimum.toPlainString().equals(mapPairs.get("ordermin"))) {
                            String[] wsNames = mapPairs.get("wsname").toString().split("/");
                            System.out.println(mapPairs.get("altname") + " - BaseID: " + sqlAssets.get(wsNames[0]) + ", QuoteID: " + sqlAssets.get(wsNames[1]) + " - Needs to be updated");
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //TODO call down all individual assets, will need to assign ID's to the inserts. Both quote and base, with fee IDs
    }
}
