import org.javatuples.Pair;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class listAssetPairs {
    private class AssetPair {
        private final String PairName;
        private double OrderMinimum;
        private String Base;
        private int LotDecimal;
        private int MarginStop;
        private int MarginCall;
        private String FeeCurrency;
        private String Quote;
        private int LotMultiplier;
        private Set<Integer> LeverageSell;
        private Set<Integer> LeverageBuy;
        private String WebsocketName;
        private int PairDecimals;
        private Set<Pair<Integer, Float>> Fees;
        private Set<Pair<Integer, Float>> FeesMaker;

        //TODO assign this when a client is assigned this pair
        private String assignedClient = "Nobody";

        //Pair<String, Integer> pair = Pair.with("Sajal", 12);
        private AssetPair(String PairName) {
            this.PairName = PairName.split(":")[1];
            fetchPairData();
        }

        private void fetchPairData() {
            //TODO use the Pairname to fetch the redis data
            Jedis jedis = new Jedis();
            List<String> returnValues = jedis.hvals("AssetPair:" + this.PairName);

            this.MarginCall = Integer.parseInt(returnValues.get(0));
            this.LotDecimal = Integer.parseInt(returnValues.get(1));
            //this.OrderMinimum = Double.parseDouble(returnValues.get(2));

            //this.FeesMaker = Integer.parseInt(returnValues.get(3));

            this.Base = returnValues.get(4);

            //this.MarginCall = Integer.parseInt(returnValues.get(5));
            this.FeeCurrency = returnValues.get(6);
            this.Quote = returnValues.get(7);
            this.LotMultiplier = Integer.parseInt(returnValues.get(8));

            //this.LeverageSell = Integer.parseInt(returnValues.get(9));
            //this.LeverageBuy = Integer.parseInt(returnValues.get(10));

            this.WebsocketName = returnValues.get(11);
            //this.PairDecimals = Integer.parseInt(returnValues.get(12));

            //this.Fees = Integer.parseInt(returnValues.get(13));
        }
    }

    private final HashMap<String, AssetPair> mapAssetPairs = new HashMap<String, AssetPair>();

    public listAssetPairs() {
        refreshAssetList();
    }

    private void refreshAssetList() {
        mapAssetPairs.clear();

        Jedis jedis = new Jedis();
        Set<String> returnKeys = jedis.keys("*AssetPair*");
        returnKeys.forEach((String strJedis) -> {
            AssetPair objPair = new AssetPair(strJedis);
            mapAssetPairs.put(strJedis, objPair);
        });
    }
}
