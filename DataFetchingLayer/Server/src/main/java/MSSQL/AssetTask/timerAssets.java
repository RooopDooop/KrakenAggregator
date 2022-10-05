package MSSQL.AssetTask;

import MSSQL.SQLConn;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class timerAssets extends TimerTask {
    @Override
    public void run() {
        System.out.println("=========================Running Asset Task=========================");

        ArrayList<Asset> JSONInsert = new ArrayList<>();
        ArrayList<Asset> JSONUpdate = new ArrayList<>();

        ArrayList<Asset> SQLAssets = new ArrayList<>();
        try {
            SQLAssets = new SQLConn().fetchAllAssets();
        } catch (SQLException e) {
            e.printStackTrace();
        }


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

                    Asset objAsset;
                    try {
                        objAsset = new Asset(
                                JSONValues.get("altname").toString(),
                                JSONValues.get("aclass").toString(),
                                (int)Double.parseDouble(JSONValues.get("decimals").toString()),
                                (int)Double.parseDouble(JSONValues.get("display_decimals").toString()),
                                new BigDecimal(JSONValues.get("collateral_value").toString())
                        );
                    } catch (NullPointerException e) {
                        objAsset = new Asset(
                                JSONValues.get("altname").toString(),
                                JSONValues.get("aclass").toString(),
                                (int)Double.parseDouble(JSONValues.get("decimals").toString()),
                                (int)Double.parseDouble(JSONValues.get("display_decimals").toString()),
                                null
                        );
                    }

                    Boolean foundAsset = false;
                    for (Asset SQLobj : SQLAssets) {
                        if (SQLobj.GetAlternativeName().equals(objAsset.GetAlternativeName())) {
                            foundAsset = true;

                            if (!objAsset.GetClass().equals(SQLobj.GetClass()) || objAsset.GetDecimals() != SQLobj.GetDecimals() || objAsset.GetDisplayDecimals() != SQLobj.GetDisplayDecimals() || !objAsset.GetCollateralValue().stripTrailingZeros().equals(SQLobj.GetCollateralValue().stripTrailingZeros()) || objAsset.GetIsFiat() != SQLobj.GetIsFiat()) {
                                System.out.println(objAsset.GetAlternativeName() + " was found to to have outdated data, Will update...");
                                JSONUpdate.add(objAsset);
                            }

                            break;
                        }
                    }

                    if (!foundAsset) {
                        System.out.println(objAsset.GetAlternativeName() + " was not found, inserting...");
                            JSONInsert.add(objAsset);
                    }
                }

                try {
                    new SQLConn().insertAssets(new Gson().toJson(JSONInsert));
                    new SQLConn().updateAssets(new Gson().toJson(JSONUpdate));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
