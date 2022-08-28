import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.javatuples.Pair;
import redis.clients.jedis.Jedis;

import java.util.*;

public class listAssetPairs {
    public class AssetPair {
        private final String PairName;
        private double OrderMinimum;
        private String Base;
        private int LotDecimal;
        private int MarginStop;
        private int MarginCall;
        private String FeeCurrency;
        private String Quote;
        private int LotMultiplier;
        private String[] LeverageSell;
        private String[] LeverageBuy;
        private String WebsocketName;
        private int PairDecimals;
        private Set<Pair<Integer, Float>> Fees;
        private Set<Pair<Integer, Float>> FeesMaker;

        //TODO assign this when a client is assigned this pair
        private WebSocket assignedConn;
        private Timer assignedTimer = new Timer();

        //Pair<String, Integer> pair = Pair.with("Sajal", 12);
        private AssetPair(String PairName) {
            this.PairName = PairName.split(":")[1];
            fetchPairData(PairName);
        }

        private void fetchPairData(String fullPairName) {
            Jedis jedis = new Jedis();

            this.Base = jedis.hget(fullPairName, "Base");
            this.OrderMinimum = Double.parseDouble(jedis.hget(fullPairName, "OrderMinimum"));
            this.LotDecimal = Integer.parseInt(jedis.hget(fullPairName, "LotDecimals"));
            this.MarginStop = Integer.parseInt(jedis.hget(fullPairName, "MarginStop"));
            this.MarginCall = Integer.parseInt(jedis.hget(fullPairName, "MarginCall"));
            this.FeeCurrency = jedis.hget(fullPairName, "FeeCurrency");
            this.Quote = jedis.hget(fullPairName, "Quote");
            this.LotMultiplier = Integer.parseInt(jedis.hget(fullPairName, "LotMultiplier"));
            this.LeverageSell = processLeverages(jedis.hget(fullPairName, "LeverageSell"));
            this.LeverageBuy = processLeverages(jedis.hget(fullPairName, "LeverageBuy"));
            this.WebsocketName = jedis.hget(fullPairName, "WebsocketName");
            this.PairDecimals = Integer.parseInt(jedis.hget(fullPairName, "PairDecimals"));
            this.Fees = processFees(jedis.hget(fullPairName, "Fees"));
            this.FeesMaker = processFees(jedis.hget(fullPairName, "FeesMaker"));
        }

        public String returnPair() {
            return this.PairName;
        }

        public WebSocket returnClient() { return this.assignedConn; }

        private String[] processLeverages(String rawLeverage) {
            String[] arrFees = {};
            return arrFees;
        }

        private Set<Pair<Integer, Float>> processFees(String rawFees) {
            Set<Pair<Integer, Float>> setLeverage = new HashSet<>();
            return setLeverage;
        }

        public boolean isAssigned() {
            return this.assignedConn != null;
        }

        public void initializeVerification(WebSocket conn) {
            this.assignedConn = conn;
            //TODO here, add

            System.out.println("Assigned timer ran: " + PairName);
            assignedTimer.scheduleAtFixedRate(new tickVerification(), 0, 5000);
        }

        class tickVerification extends TimerTask {
            public void run() {
                System.out.println("Verify pair: " + PairName);
                wsMessage objMessage = new wsMessage("VerifyPair", PairName);
                assignedConn.send(new Gson().toJson(objMessage));
            }
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

    public String returnRandomPair() {
        for (String strPair : mapAssetPairs.keySet()) {
            AssetPair objPair = mapAssetPairs.get(strPair);

            if (objPair.assignedConn == null) {
                return objPair.returnPair();
            } else {
                System.out.println("Pair is linked: " + objPair.returnPair());
            }
        }

        return null;
    }

    public void assignPairClient(String strPair, WebSocket webConn) {
        mapAssetPairs.get("AssetPair:" + strPair).initializeVerification(webConn);
    }

    public void unassignPairClient(String strPair) {
        mapAssetPairs.get("AssetPair:" + strPair).assignedConn = null;
        mapAssetPairs.get("AssetPair:" + strPair).assignedTimer.cancel();
    }

    public HashMap<String, AssetPair> returnAssignedPairs() {
        HashMap<String, AssetPair> assignedValues = new HashMap<String, AssetPair>();

        for (String strPair : mapAssetPairs.keySet()) {
            AssetPair objPair = mapAssetPairs.get(strPair);

            if (objPair.isAssigned()) {
                assignedValues.put(objPair.PairName, objPair);
            }
        }

        return assignedValues;
    }
}
