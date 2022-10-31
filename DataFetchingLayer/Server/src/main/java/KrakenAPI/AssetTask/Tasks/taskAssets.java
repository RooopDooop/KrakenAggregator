package KrakenAPI.AssetTask.Tasks;

import KrakenAPI.AssetTask.Objects.krakenAsset;
import MSSQL.Objects.sqlAsset;
import MSSQL.SQLConn;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class taskAssets extends TimerTask {
    @Override
    public void run() {
        System.out.println("=========================Running Asset Task=========================");

        ArrayList<krakenAsset> JSONInsert = new ArrayList<>();
        ArrayList<krakenAsset> JSONUpdate = new ArrayList<>();
        Map<String, krakenAsset> returnAssets = new HashMap<>();

        try {
            HttpURLConnection HTTPConn = (HttpURLConnection) new URL("https://api.kraken.com/0/public/Assets").openConnection();
            HTTPConn.setRequestMethod("GET");
            HTTPConn.setRequestProperty("Accept", "application/json");

            if (HTTPConn.getResponseCode() != 200) { throw new RuntimeException("Failed : HTTP error code : " + HTTPConn.getResponseCode());}

            BufferedReader returnData = new BufferedReader(new InputStreamReader((HTTPConn.getInputStream())));
            String output;
            while ((output = returnData.readLine()) != null) {
                for (Object JSONAsset : ((Map<String, Object>)new Gson().fromJson(output, HashMap.class).get("result")).values()) {
                    HashMap<String, Object> JSONValues = new Gson().fromJson(new Gson().toJson(JSONAsset), HashMap.class);

                    krakenAsset objAsset;
                    try {
                        objAsset = new krakenAsset(
                                JSONValues.get("altname").toString(),
                                JSONValues.get("aclass").toString(),
                                (int)Double.parseDouble(JSONValues.get("decimals").toString()),
                                (int)Double.parseDouble(JSONValues.get("display_decimals").toString()),
                                new BigDecimal(JSONValues.get("collateral_value").toString())
                        );
                    } catch (NullPointerException e) {
                        objAsset = new krakenAsset(
                                JSONValues.get("altname").toString(),
                                JSONValues.get("aclass").toString(),
                                (int)Double.parseDouble(JSONValues.get("decimals").toString()),
                                (int)Double.parseDouble(JSONValues.get("display_decimals").toString()),
                                null
                        );
                    }

                    returnAssets.put(objAsset.GetAlternativeName(), objAsset);
                }
            }

            StringBuilder AggregatedAlternatives = new StringBuilder();
            for (krakenAsset objAsset : returnAssets.values()) {
                if (AggregatedAlternatives.isEmpty()) {
                    AggregatedAlternatives.append(objAsset.GetAlternativeName());
                } else {
                    AggregatedAlternatives.append(",").append(objAsset.GetAlternativeName());
                }
            }

            Map<String, sqlAsset> mapSQLAssets = new HashMap<>();
            try {
                mapSQLAssets = new SQLConn().compareAllAssets(AggregatedAlternatives.toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }

            for (krakenAsset objAsset : returnAssets.values()) {
                sqlAsset searchSQLAsset = mapSQLAssets.get(objAsset.GetAlternativeName());

                if (searchSQLAsset.GetAssetID() == 0) {
                    JSONInsert.add(objAsset);
                } else {
                    if (!objAsset.GetClass().equals(searchSQLAsset.GetStrClass()) || objAsset.GetDecimals() != searchSQLAsset.GetDecimals() || objAsset.GetDisplayDecimals() != searchSQLAsset.GetDisplayDecimals() || !Objects.equals(objAsset.GetCollateralValue().stripTrailingZeros(), searchSQLAsset.GetCollateralValue().stripTrailingZeros()) || objAsset.GetIsFiat() != searchSQLAsset.GetIsFiat()) {
                        JSONUpdate.add(objAsset);
                    }
                }
            }

            if (JSONInsert.size() > 0) {
                System.out.println("Inserting: " + JSONInsert.size() + " Asset(s)");
                try {
                    new SQLConn().insertBulkAssets(new Gson().toJson(JSONInsert));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (JSONUpdate.size() > 0) {
                System.out.println("Updating: " + JSONUpdate.size() + " Asset(s)");
                try {
                    new SQLConn().updateBulkAssets(new Gson().toJson(JSONUpdate));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
