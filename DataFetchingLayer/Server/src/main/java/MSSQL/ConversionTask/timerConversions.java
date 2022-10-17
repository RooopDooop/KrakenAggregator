package MSSQL.ConversionTask;

import MSSQL.AssetTask.Asset;
import MSSQL.SQLConn;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.TimerTask;

public class timerConversions extends TimerTask {
    private static final String testData = "{\"success\": true,\"query\": {\"from\": \"AUD\",\"to\": \"USD\",\"amount\": 1},\"info\": {\"timestamp\": 1665370684,\"rate\": 0.636105},\"date\": \"2022-10-10\",\"result\": 0.636105}";

    @Override
    public void run() {
        System.out.println("=========================Running Conversion Task=========================");

        ArrayList<Asset> sqlAssets = new ArrayList<>();

        try {
            sqlAssets = new SQLConn().fetchAllFiatAssets();
        } catch(Exception e) {
            e.printStackTrace();
        }

        for (Asset objAsset : sqlAssets) {
            if (!objAsset.GetAlternativeName().equals("USD")) {
                try {
                    URL url = new URL("https://api.apilayer.com/exchangerates_data/convert?to=USD&from=" + objAsset.GetAlternativeName() + "&amount=1");

                    HttpURLConnection HTTPConn = (HttpURLConnection) url.openConnection();
                    HTTPConn.setRequestMethod("GET");
                    HTTPConn.setRequestProperty("Accept", "application/json");
                    HTTPConn.setRequestProperty("apikey", "REMOVED");


                    if (HTTPConn.getResponseCode() != 200) {
                        throw new RuntimeException("Failed : HTTP error code : " + HTTPConn.getResponseCode());
                    }

                    BufferedReader returnData = new BufferedReader(new InputStreamReader((HTTPConn.getInputStream())));


                    String output = "";
                    while ((returnData.readLine()) != null) {
                        //JsonReader jr = new JsonReader(new StringReader(output));
                        //jr.setLenient(true);

                        //String parsedJson = new Gson().fromJson(jr);

                        //Map keyValueMap = (Map) new Gson().fromJson(jr, Object.class);

                        //JsonObject rawConversionData = new Gson().fromJson(parsedJson, JsonObject.class);
                        //Map<String, Map> objKrakenData = new Gson().fromJson(rawConversionData, Map.class);

                        //System.out.println("From: " + objAsset.GetAlternativeName() + " - To: USD - " + rawConversionData.get("result") + " - Epoch: " + new BigDecimal(rawConversionData.get("info").getAsJsonObject().get("timestamp").toString()));
                        output += returnData;
                    }

                    System.out.println(output);



                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
