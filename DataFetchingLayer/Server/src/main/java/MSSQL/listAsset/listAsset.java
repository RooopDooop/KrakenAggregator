package MSSQL.listAsset;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class listAsset {
    protected HashMap<String, Asset> hashAssets = new HashMap<>();
    public listAsset() {
        fetchAssets();
    }

    public HashMap<String, Asset> getAssets() {
        return hashAssets;
    }

    private void fetchAssets() {
        try {
            URL url = new URL("https://api.kraken.com/0/public/Assets");
            HttpURLConnection HTTPConn = (HttpURLConnection) url.openConnection();
            HTTPConn.setRequestMethod("GET");
            HTTPConn.setRequestProperty("Accept", "application/json");

            if (HTTPConn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + HTTPConn.getResponseCode());
            }

            BufferedReader returnData = new BufferedReader(new InputStreamReader((HTTPConn.getInputStream())));

            String output;
            while ((output = returnData.readLine()) != null) {
                new Gson().fromJson(new Gson().fromJson(output, HashMap.class).get("result").toString(), HashMap.class).forEach((key, value) -> {
                    JsonObject jsonObject = new Gson().toJsonTree(value).getAsJsonObject();

                    String strClass = jsonObject.get("aclass").toString().replace("\"", "");
                    String strAlt= jsonObject.get("altname").toString().replace("\"", "");
                    Integer intDecimals = jsonObject.get("decimals").getAsInt();
                    Integer intDisplay= jsonObject.get("display_decimals").getAsInt();
                    BigDecimal decimalCollateral = new BigDecimal(0);

                    if (jsonObject.get("collateral_value") == null) {
                        decimalCollateral = new BigDecimal(0);
                    } else {
                        decimalCollateral = new BigDecimal(jsonObject.get("collateral_value").toString());
                    }

                    hashAssets.put(strAlt, new Asset(strClass, strAlt, intDecimals, intDisplay, decimalCollateral));
                });
            }

            HTTPConn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
