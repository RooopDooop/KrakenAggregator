package MSSQL.PairTask;
import MSSQL.AssetTask.Asset;
import MSSQL.SQLConn;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.javatuples.Octet;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Array;
import java.sql.SQLException;
import java.util.*;

public class timerPairs extends TimerTask {
    @Override
    public void run() {
        System.out.println("=========================Running Pair Task=========================");

        Map<String, Octet<String, Integer, Integer, Integer, Integer, Integer, Integer, BigDecimal>> sqlPairs = new HashMap<>();

        StringBuilder JSONInsert = new StringBuilder().append("[");
        StringBuilder JSONUpdate = new StringBuilder().append("[");

        ArrayList<Fee> hashFees = new ArrayList<>();
        Map<Integer, ArrayList<Integer>> hashLeverageBuy = new HashMap<>();
        Map<Integer, ArrayList<Integer>> hashLeverageSell = new HashMap<>();

        try {
            sqlPairs = new SQLConn().fetchPairs();
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
                    if (sqlPairs.get(mapPairs.get("altname").toString()) == null) {
                        String[] wsNames = mapPairs.get("wsname").toString().split("/");

                        try {
                            int BaseID = new SQLConn().fetchAssetID(wsNames[0]);
                            int QuoteID = new SQLConn().fetchAssetID(wsNames[1]);
                            int CurrencyID = new SQLConn().fetchAssetID("USD");

                            System.out.println(BaseID + " - " + QuoteID + " : " + CurrencyID);

                            if (JSONInsert.toString().equals("[")) {
                                //TODO, need to check the websocket names
                                JSONInsert = new StringBuilder("[{\"AlternativePairName\": \"" + mapPairs.get("altname") +
                                        "\", \"WebsocketPairName\": \"" + mapPairs.get("wsname") +
                                        "\", \"BaseID\": " + BaseID +
                                        ", \"QuoteID\": " + QuoteID +
                                        ", \"PairDecimals\": " + (int)Double.parseDouble(mapPairs.get("pair_decimals").toString()) +
                                        ", \"LotDecimals\": " + (int)Double.parseDouble(mapPairs.get("lot_decimals").toString()) +
                                        ", \"LotMultiplier\": " + (int)Double.parseDouble(mapPairs.get("lot_multiplier").toString()) +
                                        ", \"FeeCurrency\": " + CurrencyID +
                                        ", \"MarginCall\": " + (int)Double.parseDouble(mapPairs.get("margin_call").toString()) +
                                        ", \"MarginStop\": " + (int)Double.parseDouble(mapPairs.get("margin_stop").toString()) +
                                        ", \"OrderMinimum\": " + mapPairs.get("ordermin") + "}");
                            } else {
                                JSONInsert.append(", {\"AlternativePairName\": \"" + mapPairs.get("altname") +
                                        "\", \"WebsocketPairName\": \"" + mapPairs.get("wsname") +
                                        "\", \"BaseID\": " + BaseID +
                                        ", \"QuoteID\": " + QuoteID +
                                        ", \"PairDecimals\": " + (int)Double.parseDouble(mapPairs.get("pair_decimals").toString()) +
                                        ", \"LotDecimals\": " + (int)Double.parseDouble(mapPairs.get("lot_decimals").toString()) +
                                        ", \"LotMultiplier\": " + (int)Double.parseDouble(mapPairs.get("lot_multiplier").toString()) +
                                        ", \"FeeCurrency\": " + CurrencyID +
                                        ", \"MarginCall\": " + (int)Double.parseDouble(mapPairs.get("margin_call").toString()) +
                                        ", \"MarginStop\": " + (int)Double.parseDouble(mapPairs.get("margin_stop").toString()) +
                                        ", \"OrderMinimum\": " + mapPairs.get("ordermin") + "}");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Octet<String, Integer, Integer, Integer, Integer, Integer, Integer, BigDecimal> objSQL = sqlPairs.get(mapPairs.get("altname").toString());

                        int PairDecimals = objSQL.getValue2();
                        int LotDecimals = objSQL.getValue3();
                        int LotMultiplier = objSQL.getValue4();
                        int MarginCall = objSQL.getValue5();
                        int MarginStop = objSQL.getValue6();
                        BigDecimal OrderMinimum = new BigDecimal(objSQL.getValue7().toString()).stripTrailingZeros();

                        if (PairDecimals != (double)mapPairs.get("pair_decimals") || LotDecimals != (double)mapPairs.get("lot_decimals") || LotMultiplier != (double)mapPairs.get("lot_multiplier") || MarginCall != (double)mapPairs.get("margin_call") || MarginStop != (double)mapPairs.get("margin_stop") || !OrderMinimum.toPlainString().equals(mapPairs.get("ordermin"))) {
                            if (JSONUpdate.toString().equals("[")) {
                                JSONUpdate = new StringBuilder("[{\"AlternativePairName\": \"" + mapPairs.get("altname") +
                                        "\", \"PairDecimals\": " + (int)Double.parseDouble(mapPairs.get("pair_decimals").toString()) +
                                        ", \"LotDecimals\": " + (int)Double.parseDouble(mapPairs.get("lot_decimals").toString()) +
                                        ", \"LotMultiplier\": " + (int)Double.parseDouble(mapPairs.get("lot_multiplier").toString())+
                                        ", \"MarginCall\": " + (int)Double.parseDouble(mapPairs.get("margin_call").toString()) +
                                        ", \"MarginStop\": " + (int)Double.parseDouble(mapPairs.get("margin_stop").toString()) +
                                        ", \"OrderMinimum\": " + mapPairs.get("ordermin") + "}");
                            } else {
                                JSONUpdate.append(", {\"AlternativePairName\": \"" + mapPairs.get("altname") +
                                        "\", \"PairDecimals\": " + (int)Double.parseDouble(mapPairs.get("pair_decimals").toString()) +
                                        ", \"LotDecimals\": " + (int)Double.parseDouble(mapPairs.get("lot_decimals").toString()) +
                                        ", \"LotMultiplier\": " + (int)Double.parseDouble(mapPairs.get("lot_multiplier").toString())+
                                        ", \"MarginCall\": " + (int)Double.parseDouble(mapPairs.get("margin_call").toString()) +
                                        ", \"MarginStop\": " + (int)Double.parseDouble(mapPairs.get("margin_stop").toString()) +
                                        ", \"OrderMinimum\": " + mapPairs.get("ordermin") + "}");
                            }
                        }
                    }

                    try {
                        Integer PairID = new SQLConn().fetchPairID(mapPairs.get("altname").toString());

                        ArrayList<ArrayList> tempFees = (ArrayList<ArrayList>)mapPairs.get("fees");
                        for (int p = 0; p < tempFees.size(); p++) {
                            Fee objFee = new Fee(
                                    PairID,
                                    "Standard",
                                    (int)Double.parseDouble(tempFees.get(p).get(0).toString()),
                                    new BigDecimal(tempFees.get(p).get(1).toString())
                            );

                            hashFees.add(objFee);
                        }


                        ArrayList<ArrayList> tempFeesMaker = (ArrayList<ArrayList>)mapPairs.get("fees_maker");
                        for (int p = 0; p < tempFeesMaker.size(); p++) {
                            Fee objFee = new Fee(
                                    PairID,
                                    "Maker",
                                    (int)Double.parseDouble(tempFees.get(p).get(0).toString()),
                                    new BigDecimal(tempFees.get(p).get(1).toString())
                            );

                            hashFees.add(objFee);
                            ArrayList<Integer> tempLeverageBuy = (ArrayList<Integer>)mapPairs.get("leverage_buy");
                            if (tempLeverageBuy.size() > 0) {
                                hashLeverageBuy.put(PairID, tempLeverageBuy);
                            }

                            ArrayList<Integer> tempLeverageSell = (ArrayList<Integer>)mapPairs.get("leverage_sell");
                            if (tempLeverageSell.size() > 0) {
                                hashLeverageSell.put(PairID, tempLeverageSell);
                            }
                        }
                    } catch (SQLException e) {
                        if (e.getMessage().equals("The result set has no current row.")) {
                            System.out.println("PairID for: " + mapPairs.get("altname") + " - Was not found, skipping pair fee/leverage inserts");
                        } else {
                            e.printStackTrace();
                        }
                    }
                }

                JSONInsert.append("]");
                JSONUpdate.append("]");

                if (!JSONInsert.toString().equals("[]")) {
                    System.out.println("Inserting list: " + JSONInsert.toString());

                    try {
                        new SQLConn().insertPairs(JSONInsert.toString());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                if (!JSONUpdate.toString().equals("[]")) {
                    System.out.println("Update list: " + JSONUpdate.toString());

                    try {
                        new SQLConn().updatePairs(JSONUpdate.toString());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                processFees(hashFees);
                processLeverages(hashLeverageBuy, false);
                processLeverages(hashLeverageSell, true);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processFees(ArrayList<Fee> Fees) {
        try {
            ArrayList<Fee> insertFees = new ArrayList<>();
            ArrayList<Fee> updateFees = new ArrayList<>();
            Map<Integer, Fee> sqlFees = new SQLConn().fetchFees();

            for (Fee objFee : Fees) {
                boolean foundFee = false;

                for (Fee objSQLFees : sqlFees.values()) {
                    if (objFee.GetPairID() == objSQLFees.GetPairID() && objFee.GetVolume() == objSQLFees.GetVolume() && objFee.GetFeeType().equals(objSQLFees.GetFeeType())) {


                        if (!objFee.GetPercentCost().stripTrailingZeros().equals(objSQLFees.GetPercentCost().stripTrailingZeros())) {
                            updateFees.add(objFee);
                            break;
                        }

                        foundFee = true;
                        break;
                    }
                }

                if (!foundFee) {
                    insertFees.add(objFee);
                }
            }

            new SQLConn().insertFees(new Gson().toJson(insertFees));
            new SQLConn().updateFees(new Gson().toJson(updateFees));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void processLeverages(Map<Integer, ArrayList<Integer>> Leverages, Boolean IsSell) {
        try {
            if (IsSell) {
                new SQLConn().insertLeverages(new Gson().toJson(Leverages), "Sell");
            } else {
                new SQLConn().insertLeverages(new Gson().toJson(Leverages), "Buy");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
