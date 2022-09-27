package MSSQL.listPair;

import MSSQL.SQLConn;
import MSSQL.listAsset.Asset;
import MSSQL.listAsset.listAsset;
import com.google.gson.Gson;
import org.javatuples.Pair;
import org.javatuples.Septet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;

public class listPair {
    private static Timer AssetPullTimer = null;
    private static HashMap<String, AssetPair> hashPairs = new HashMap<>();

    public listPair() {
        if (AssetPullTimer == null) {
            System.out.println("Generated listPair class");
            AssetPullTimer = new Timer();
            AssetPullTimer.scheduleAtFixedRate(new timerPairRefresh(), 0, 100000);
        }
    }

    public HashMap<String, AssetPair> getSQLPairs() {
        //TODO get pairs from mssql
        return this.hashPairs;
    }

    private void WritePairsToDB() {
       /* System.out.println("Writing pairs to DB");

        ArrayList<Septet<String, Integer, Integer, Integer, Integer, Integer, BigDecimal>> listTupleValues = new ArrayList<>();

        StringBuilder JSONInsert = new StringBuilder();
        String JSONUpdate = "";

        try {
            listTupleValues = new SQLConn().fetchPairs();
        } catch(Exception e) {
            e.printStackTrace();
        }

        for (AssetPair objPair : hashPairs.values()) {
            boolean foundPair = false;
            boolean valueStale = false;

            for (Septet<String, Integer, Integer, Integer, Integer, Integer, BigDecimal> tupleSQLPairs : listTupleValues) {
                if (tupleSQLPairs.getValue0().equals(objPair.getAlternativeName())) {
                    if (!Objects.equals(tupleSQLPairs.getValue1(), objPair.getPairDecimals())) {
                        valueStale = true;
                        break;
                    }

                    if (!Objects.equals(tupleSQLPairs.getValue2(), objPair.getLotDecimals())) {
                        valueStale = true;
                        break;
                    }

                    if (!Objects.equals(tupleSQLPairs.getValue3(), objPair.getLotMultiplier())) {
                        valueStale = true;
                        break;
                    }

                    if (!Objects.equals(tupleSQLPairs.getValue4(), objPair.getMarginCall())) {
                        valueStale = true;
                        break;
                    }

                    if (!Objects.equals(tupleSQLPairs.getValue5(), objPair.getMarginStop())) {
                        valueStale = true;
                        break;
                    }

                    if (!Objects.equals(new BigDecimal(tupleSQLPairs.getValue6().toString()).stripTrailingZeros().toPlainString(), objPair.getOrderMinimum().toString())) {
                        valueStale = true;
                        break;
                    }

                    foundPair = true;
                    break;
                }
            }

            if (!foundPair) {
                //TODO add to list of insert functions
                System.out.println("Not found: " + objPair.getAlternativeName());

                if (JSONInsert.toString().equals("")) {
                    JSONInsert = new StringBuilder("[{\"AlternativePairName\": " + objPair.getAlternativeName() +
                            ", \"WebsocketPairName\": " + objPair.getWebsocketName() +
                            ", \"BaseID\": " + objPair.getBaseAssetID() +
                            ", \"QuoteID\": " + objPair.getQuoteAssetID() +
                            ", \"PairDecimals\": " + objPair.getPairDecimals() +
                            ", \"LotDecimals\": " + objPair.getLotDecimals() +
                            ", \"LotMultiplier\": " + objPair.getLotMultiplier() +
                            ", \"FeeCurrency\": " + objPair.getFeeAsset() +
                            ", \"MarginCall\": " + objPair.getMarginCall() +
                            ", \"MarginStop\": " + objPair.getMarginStop() +
                            ", \"OrderMinimum\": " + objPair.getOrderMinimum() + "}");
                } else {
                    JSONInsert.append(", {\"AlternativePairName\": ").append(objPair.getAlternativeName()).append(", \"WebsocketPairName\": ").append(objPair.getWebsocketName()).append(", \"BaseID\": ").append(objPair.getBaseAssetID()).append(", \"QuoteID\": ").append(objPair.getQuoteAssetID()).append(", \"PairDecimals\": ").append(objPair.getPairDecimals()).append(", \"LotDecimals\": ").append(objPair.getLotDecimals()).append(", \"LotMultiplier\": ").append(objPair.getLotMultiplier()).append(", \"FeeCurrency\": ").append(objPair.getFeeAsset()).append(", \"MarginCall\": ").append(objPair.getMarginCall()).append(", \"MarginStop\": ").append(objPair.getMarginStop()).append(", \"OrderMinimum\": ").append(objPair.getOrderMinimum()).append("}");
                }
            }

            if (valueStale) {
                //TODO add to list of update functions
                System.out.println("Stale values: " + objPair.getAlternativeName());
            }
        }

        if (!JSONInsert.toString().equals("")) {
            JSONInsert.append("]");
        }


        System.out.println(JSONInsert.toString());*/

            //TODO call MSSQL twice once for inserts if json insert is longer than 0
            //Then another time for json updates if longer than 0
    }

    class timerPairRefresh extends TimerTask {
        @Override
        public void run() {
            System.out.println("Running asset timer job");

            try {
                URL url = new URL("https://api.kraken.com/0/public/AssetPairs");

                HttpURLConnection HTTPConn = (HttpURLConnection) url.openConnection();
                HTTPConn.setRequestMethod("GET");
                HTTPConn.setRequestProperty("Accept", "application/json");

                if (HTTPConn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : " + HTTPConn.getResponseCode());
                }

                BufferedReader returnData = new BufferedReader(new InputStreamReader((HTTPConn.getInputStream())));

                String output;
                while ((output = returnData.readLine()) != null) {
                    //System.out.println(new Gson().fromJson(output, HashMap.class).get("result").toString());

                    new Gson().fromJson(new Gson().toJson(new Gson().fromJson(output, HashMap.class).get("result")) , HashMap.class).forEach((key, value) -> {
                        HashMap<String, Object> rawPairData = new Gson().fromJson(new Gson().toJson(value), HashMap.class);

                        int decimals;
                        int lotDecimals;
                        int lotMultiplier;
                        int margin_call;
                        int margin_stop;

                        try{
                            decimals = (int) Math.round((Double)  rawPairData.get("pair_decimals"));
                            lotDecimals = (int) Math.round((Double)  rawPairData.get("lot_decimals"));
                            lotMultiplier = (int) Math.round((Double)  rawPairData.get("lot_multiplier"));
                            margin_call = (int) Math.round((Double)  rawPairData.get("margin_call"));
                            margin_stop = (int) Math.round((Double)  rawPairData.get("margin_stop"));
                        }
                        catch (NumberFormatException e){
                            //TODO write stack to MSSQL
                            e.printStackTrace();
                            return;
                        }

                        String[] arrWSStr = rawPairData.get("wsname").toString().split("/", 2);
                        /*AssetPair objAssetPair = new AssetPair(
                                rawPairData.get("altname").toString(),
                                rawPairData.get("wsname").toString(),
                                arrWSStr[0],
                                arrWSStr[1],
                                decimals,
                                lotDecimals,
                                lotMultiplier,
                                rawPairData.get("fee_volume_currency").toString(),
                                margin_call,
                                margin_stop,
                                new BigDecimal(rawPairData.get("ordermin").toString()),
                                rawPairData.get("fees"),
                                rawPairData.get("fees_maker"),
                                rawPairData.get("leverage_buy"),
                                rawPairData.get("leverage_sell")
                        );*/

                        //TODO remove this, get from MSSQL
                        //hashPairs.put(rawPairData.get("altname").toString(), objAssetPair);
                    });
                }

                WritePairsToDB();
                System.out.println("Asset job insert completed");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
