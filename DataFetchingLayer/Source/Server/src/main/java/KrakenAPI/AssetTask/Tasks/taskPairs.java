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

        //Map<String, Octet<String, Integer, Integer, Integer, Integer, Integer, Integer, BigDecimal>> sqlPairs = new HashMap<>();
        StringBuilder JSONInsert = new StringBuilder().append("[");
        StringBuilder JSONUpdate = new StringBuilder().append("[");

        ArrayList<krakenFee> arrFees = new ArrayList<>();
        Map<Integer, ArrayList<Integer>> hashLeverageBuy = new HashMap<>();
        Map<Integer, ArrayList<Integer>> hashLeverageSell = new HashMap<>();


        Map<String, krakenPair> krakenPairs = new HashMap<>();
        Map<String, Integer> existingAssets = new HashMap<>();

        /*try {
            existingAssets = new SQLConn().fetchAllAssetIDs();
        } catch (SQLException e) {
            e.printStackTrace();
        }*/

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

                //StringBuilder AggregatedAlternativeNames = new StringBuilder();

                /*for (krakenPair objPair : krakenPairs.values()) {
                    if (AggregatedAlternativeNames.toString().equals("")) {
                        AggregatedAlternativeNames = new StringBuilder(objPair.returnAlternativeName());
                    } else {
                        AggregatedAlternativeNames.append(",").append(objPair.returnAlternativeName());
                    }
                }*/

                /*Map<String, sqlPair> mapSQLPairs = new HashMap<>();
                try {
                    mapSQLPairs = new SQLConn().compareAllPairs(AggregatedAlternativeNames.toString());
                } catch (SQLException e) {
                    e.printStackTrace();
                }*/

                for (krakenPair objPair : krakenPairs.values()) {
                    String[] websocketName = objPair.returnWebsocketName().split("/");

                    try {
                        objPair.WriteToMongo(websocketName[0]);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }




                    /*sqlPair searchPair = mapSQLPairs.get(objPair.returnAlternativeName());

                    try {
                        if (searchPair.GetPairID() == 0) {
                            if (JSONInsert.toString().equals("[")) {
                                JSONInsert.append(objPair.ProcessJSON(existingAssets.get(websocketName[0]), existingAssets.get(websocketName[1]), existingAssets.get(objPair.returnCurrencyAsset().toString().replace("Z", ""))));
                            } else {
                                JSONInsert.append(", ").append(objPair.ProcessJSON(existingAssets.get(websocketName[0]), existingAssets.get(websocketName[1]), existingAssets.get(objPair.returnCurrencyAsset().toString().replace("Z", ""))));
                            }

                        } else {
                            if (objPair.returnPairDecimals() != searchPair.returnPairDecimals() || objPair.returnLotDecimals() != searchPair.returnLotDecimals() || objPair.returnLotMultipliers() != searchPair.returnLotMultipliers() || objPair.returnMarginCall() != searchPair.returnMarginCall() || objPair.returnMarginStop() != searchPair.returnMarginStop() || !Objects.equals(objPair.returnOrderMinimum().stripTrailingZeros(), searchPair.returnOrderMinimum().stripTrailingZeros()) || !Objects.equals(objPair.returnCostMinimum().stripTrailingZeros(), searchPair.returnCostMinimum().stripTrailingZeros())) {
                                if (JSONUpdate.toString().equals("[")) {
                                    JSONUpdate.append(objPair.ProcessJSON(existingAssets.get(websocketName[0]), existingAssets.get(websocketName[1]), existingAssets.get(objPair.returnCurrencyAsset().toString().replace("Z", ""))));
                                } else {
                                    JSONUpdate.append(", ").append(objPair.ProcessJSON(existingAssets.get(websocketName[0]), existingAssets.get(websocketName[1]), existingAssets.get(objPair.returnCurrencyAsset().toString().replace("Z", ""))));
                                }
                            }

                            if (objPair.returnLeverageBuy().size() > 0) {
                                hashLeverageBuy.put(searchPair.GetPairID(), objPair.returnLeverageBuy());
                            }

                            if (objPair.returnLeverageSell().size() > 0) {
                                hashLeverageSell.put(searchPair.GetPairID(), objPair.returnLeverageSell());
                            }

                            arrFees.addAll(objPair.returnFees(searchPair.GetPairID()));
                        }
                    } catch (NullPointerException e) {
                        break;
                    }*/
                }

                /*JSONInsert.append("]");
                JSONUpdate.append("]");

                try {
                    new SQLConn().insertBulkPairs(JSONInsert.toString());
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                try {
                    new SQLConn().updateBulkPairs(JSONUpdate.toString());
                } catch (SQLException e) {
                    e.printStackTrace();
                }*/

                /*processFees(arrFees);

                processLeverages(hashLeverageBuy, false);
                processLeverages(hashLeverageSell, true);*/
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*private void processFees(ArrayList<krakenFee> Fees) {
        try {
            ArrayList<krakenFee> insertFees = new ArrayList<>();
            ArrayList<krakenFee> updateFees = new ArrayList<>();
            Map<Integer, sqlFees> mapSQLFees = new SQLConn().fetchFees();

            for (krakenFee objKrakenFee : Fees) {
                boolean foundValue = false;

                for (sqlFees SQLFees : mapSQLFees.values()) {
                    if (SQLFees.returnPairID() == objKrakenFee.GetPairID() && SQLFees.returnFeeType().equals(objKrakenFee.GetFeeType()) && SQLFees.returnFeeVolume() == objKrakenFee.GetVolume()) {
                        foundValue = true;

                        if (SQLFees.returnFeePercentCost() != objKrakenFee.GetPercentCost()) {
                            updateFees.add(objKrakenFee);
                        }
                        break;
                    }
                }

                if (!foundValue) {
                    insertFees.add(objKrakenFee);
                }
            }

            try {
                new SQLConn().insertBulkFees(new Gson().toJson(insertFees));
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                new SQLConn().updateBulkFees(new Gson().toJson(updateFees));
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/

    /*private void processLeverages(Map<Integer, ArrayList<Integer>> Leverages, Boolean IsSell) {
        StringBuilder strRemoveLeverages = new StringBuilder("[");
        StringBuilder strAddLeverages = new StringBuilder("[");

        for (Integer keyLeverages : Leverages.keySet()) {
            ArrayList<Integer> defaultLeverages = new ArrayList<>(Arrays.asList(2,3,4,5));
            ArrayList<Integer> rawLeverages = Leverages.get(keyLeverages);

            for (Integer defaultLeverage : defaultLeverages) {
                if (!rawLeverages.contains(defaultLeverage)) {
                    if (!IsSell) {
                        if (strRemoveLeverages.toString().equals("[")) {
                            strRemoveLeverages.append("{\"PairID\": ").append(keyLeverages).append(", \"LeverageValue\": ").append(defaultLeverage).append(", \"LeverageType\": \"Buy\"}");
                        } else {
                            strRemoveLeverages.append(", {\"PairID\": ").append(keyLeverages).append(", \"LeverageValue\": ").append(defaultLeverage).append(", \"LeverageType\": \"Buy\"}");
                        }
                    } else {
                        if (strRemoveLeverages.toString().equals("[")) {
                            strRemoveLeverages.append("{\"PairID\": ").append(keyLeverages).append(", \"LeverageValue\": ").append(defaultLeverage).append(", \"LeverageType\": \"Sell\"}");
                        } else {
                            strRemoveLeverages.append(", {\"PairID\": ").append(keyLeverages).append(", \"LeverageValue\": ").append(defaultLeverage).append(", \"LeverageType\": \"Sell\"}");
                        }
                    }
                }
            }

            for (Integer LeverageValue : rawLeverages) {
                if (!IsSell) {
                    if (strAddLeverages.toString().equals("[")) {
                        strAddLeverages.append("{\"PairID\": ").append(keyLeverages).append(", \"LeverageValue\": ").append(LeverageValue).append(", \"LeverageType\": \"Buy\"}");
                    } else {
                        strAddLeverages.append(", {\"PairID\": ").append(keyLeverages).append(", \"LeverageValue\": ").append(LeverageValue).append(", \"LeverageType\": \"Buy\"}");
                    }

                } else {
                    if (strAddLeverages.toString().equals("[")) {
                        strAddLeverages.append("{\"PairID\": ").append(keyLeverages).append(", \"LeverageValue\": ").append(LeverageValue).append(", \"LeverageType\": \"Sell\"}");
                    } else {
                        strAddLeverages.append(", {\"PairID\": ").append(keyLeverages).append(", \"LeverageValue\": ").append(LeverageValue).append(", \"LeverageType\": \"Sell\"}");
                    }
                }
            }
        }

        strRemoveLeverages.append("]");
        strAddLeverages.append("]");

        try {
            new SQLConn().insertBulkLeverages(strAddLeverages.toString());
            new SQLConn().deleteBulkLeverages(strRemoveLeverages.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/
}
