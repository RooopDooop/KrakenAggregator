import org.javatuples.Pair;
import redis.clients.jedis.Jedis;

import java.util.HashSet;
import java.util.Set;

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

    public AssetPair(String PairName) {
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

    private String[] processLeverages(String rawLeverage) {
        String[] arrFees = {};
        return arrFees;
    }

    private Set<Pair<Integer, Float>> processFees(String rawFees) {
        Set<Pair<Integer, Float>> setLeverage = new HashSet<>();
        return setLeverage;
    }
}
