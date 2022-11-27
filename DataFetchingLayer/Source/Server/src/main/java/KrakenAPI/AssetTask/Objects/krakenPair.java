package KrakenAPI.AssetTask.Objects;

import Mongo.MongoConn;
import com.google.gson.internal.LinkedTreeMap;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.javatuples.Pair;

import java.math.BigDecimal;
import java.util.ArrayList;

import static com.mongodb.client.model.Filters.eq;

public class krakenPair {
    private String altName;
    private String wsname;
    private String aclass_base;
    private String base;
    private String a_class_quote;
    private String quote;
    private String lot;
    private int cost_decimals;
    private int pair_decimals;
    private int lot_decimals;
    private int lot_multiplier;
    private ArrayList<Integer> leverage_buy = new ArrayList<>();
    private ArrayList<Integer> leverage_sell = new ArrayList<>();
    private ArrayList<Document> fees = new ArrayList<>();
    private ArrayList<Document> fees_maker = new ArrayList<>();
    private String fee_volume_currency;
    private int margin_call;
    private int margin_stop;
    private BigDecimal ordermin;
    private BigDecimal costmin;
    private BigDecimal tick_size;
    private String status;

    public krakenPair(LinkedTreeMap<String, Object> creationData) throws ClassCastException {
        for (String key : creationData.keySet()) {
            switch (key) {
                case "altname" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.lang.String")) {
                        throw new ClassCastException("altname was not the correct data type");
                    }
                    this.altName = (String) creationData.get(key);
                }
                case "wsname" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.lang.String")) {
                        throw new ClassCastException("wsname was not the correct data type");
                    }
                    this.wsname = (String) creationData.get(key);
                }
                case "aclass_base" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.lang.String")) {
                        throw new ClassCastException("aclass_base was not the correct data type");
                    }
                    this.aclass_base = (String) creationData.get(key);
                }
                case "base" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.lang.String")) {
                        throw new ClassCastException("base was not the correct data type");
                    }
                    this.base = (String) creationData.get(key);
                }
                case "aclass_quote" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.lang.String")) {
                        throw new ClassCastException("aclass_quote was not the correct data type");
                    }
                    this.a_class_quote = (String) creationData.get(key);
                }
                case "quote" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.lang.String")) {
                        throw new ClassCastException("quote was not the correct data type");
                    }
                    this.quote = (String) creationData.get(key);
                }
                case "lot" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.lang.String")) {
                        throw new ClassCastException("lot was not the correct data type");
                    }
                    this.lot = (String) creationData.get(key);
                }
                case "cost_decimals" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.lang.Double")) {
                        throw new ClassCastException("cost_decimals was not the correct data type");
                    }
                    Double doubleCostDecimals = (Double) creationData.get(key);
                    this.cost_decimals = doubleCostDecimals.intValue();
                }
                case "pair_decimals" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.lang.Double")) {
                        throw new ClassCastException("pair_decimals was not the correct data type");
                    }
                    Double doublePairDecimals = (Double) creationData.get(key);
                    this.pair_decimals = doublePairDecimals.intValue();
                }
                case "lot_decimals" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.lang.Double")) {
                        throw new ClassCastException("lot_decimals was not the correct data type");
                    }
                    Double doubleLotDecimals = (Double) creationData.get(key);
                    this.lot_decimals = doubleLotDecimals.intValue();
                }
                case "lot_multiplier" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.lang.Double")) {
                        throw new ClassCastException("lot_multiplier was not the correct data type");
                    }
                    Double doubleLotMultiplier = (Double) creationData.get(key);
                    this.lot_multiplier = doubleLotMultiplier.intValue();
                }
                case "leverage_buy" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.util.ArrayList")) {
                        throw new ClassCastException("leverage_buy was not the correct data type");
                    }
                    for (Double doubleLeverageBuy : (ArrayList<Double>) creationData.get(key)) {
                        this.leverage_buy.add(doubleLeverageBuy.intValue());
                    }
                }
                case "leverage_sell" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.util.ArrayList")) {
                        throw new ClassCastException("leverage_sell was not the correct data type");
                    }
                    for (Double doubleLeverageSell : (ArrayList<Double>) creationData.get(key)) {
                        this.leverage_sell.add(doubleLeverageSell.intValue());
                    }
                }
                case "fees" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.util.ArrayList")) {
                        throw new ClassCastException("fees was not the correct data type");
                    }
                    for (Object objFee : (ArrayList) creationData.get(key)) {
                        int Volume = ((ArrayList<Double>) objFee).get(0).intValue();
                        Double Percentage = ((ArrayList<Double>) objFee).get(1);

                        this.fees.add(new Document("Volume", Volume).append("Percentage", Percentage));;
                    }
                }
                case "fees_maker" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.util.ArrayList")) {
                        throw new ClassCastException("fees_maker was not the correct data type");
                    }
                    for (Object objFee : (ArrayList) creationData.get(key)) {
                        int Volume = ((ArrayList<Double>) objFee).get(0).intValue();
                        Double Percentage = ((ArrayList<Double>) objFee).get(1);

                        this.fees_maker.add(new Document("Volume", Volume).append("Percentage", Percentage));
                    }
                }
                case "fee_volume_currency" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.lang.String")) {
                        throw new ClassCastException("fee_volume_currency was not the correct data type");
                    }
                    this.fee_volume_currency = (String) creationData.get(key);
                }
                case "margin_call" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.lang.Double")) {
                        throw new ClassCastException("margin_call was not the correct data type");
                    }
                    Double doubleMarginCall = (Double) creationData.get(key);
                    this.margin_call = doubleMarginCall.intValue();
                }
                case "margin_stop" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.lang.Double")) {
                        throw new ClassCastException("margin_stop was not the correct data type");
                    }
                    Double doubleMarginStop = (Double) creationData.get(key);
                    this.margin_stop = doubleMarginStop.intValue();
                }
                case "ordermin" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.lang.String")) {
                        throw new ClassCastException("ordermin was not the correct data type");
                    }
                    this.ordermin = new BigDecimal(creationData.get(key).toString());
                }
                case "costmin" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.lang.String")) {
                        throw new ClassCastException("costmin was not the correct data type");
                    }
                    this.costmin = new BigDecimal(creationData.get(key).toString());
                }
                case "tick_size" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.lang.String")) {
                        throw new ClassCastException("tick_size was not the correct data type");
                    }
                    this.tick_size = new BigDecimal(creationData.get(key).toString());
                }
                case "status" -> {
                    if (!creationData.get(key).getClass().getName().equals("java.lang.String")) {
                        throw new ClassCastException("status was not the correct data type");
                    }
                    this.status = creationData.get(key).toString();
                }
            }

            if (creationData.get("costmin") == null) {
                this.costmin = new BigDecimal(0);
            }
        }
    }

    public String returnAlternativeName() {
        return this.altName;
    }
    public String returnWebsocketName() {
        return this.wsname;
    }
    public String returnCurrencyAsset() { return this.fee_volume_currency; }
    public int returnPairDecimals() { return this.pair_decimals; }
    public int returnLotDecimals() { return this.lot_decimals; }
    public int returnLotMultipliers() { return this.lot_multiplier; }
    public int returnMarginCall() { return this.margin_call; }
    public int returnMarginStop() { return this.margin_stop; }
    public BigDecimal returnOrderMinimum() { return this.ordermin; }
    public BigDecimal returnCostMinimum() { return this.costmin; }
    public ArrayList<Integer> returnLeverageBuy() { return this.leverage_buy; }
    public ArrayList<Integer> returnLeverageSell() { return this.leverage_sell; }
    public ArrayList<krakenFee> returnFees(int PairID) {
        ArrayList<krakenFee> returnList = new ArrayList<>();

        for (Object objRawFee : this.fees.toArray()) {
            Pair<Integer, Double> objFee = (Pair<Integer, Double>) objRawFee;
            returnList.add(new krakenFee(PairID, "Standard", objFee.getValue0(), objFee.getValue1()));
        }

        for (Object objRawFeeMaker : this.fees_maker.toArray()) {
            Pair<Integer, Double> objFee = (Pair<Integer, Double>) objRawFeeMaker;
            returnList.add(new krakenFee(PairID, "Maker", objFee.getValue0(), objFee.getValue1()));
        }

        return returnList;
    }

    public void WriteToMongo(String wsName) throws Exception {
        MongoCollection mongoCollection = MongoConn.getMongo().getDatabase("KrakenDB").getCollection("Assets");

        FindIterable mongoIterator = mongoCollection.find(eq("_id", wsName));

        if (mongoIterator.first() == null) {
            throw new Exception("Error, asset not found on database");
        }

        Document BSONPair = new Document("AlternativePairName", this.altName)
                                .append("WebsocketPairName", this.wsname)
                                .append("PairDecimals", this.pair_decimals)
                                .append("LotDecimals", this.lot_decimals)
                                .append("LotMultiplier", this.lot_multiplier)
                                .append("FeeCurrency", this.fee_volume_currency)
                                .append("MarginCall", this.margin_call)
                                .append("MarginStop", this.margin_stop)
                                .append("OrderMinimum", this.ordermin)
                                .append("CostMinimum", this.costmin)
                                .append("LeverageBuy", leverage_buy)
                                .append("LeverageSell", leverage_sell)
                                .append("Fees", fees)
                                .append("Maker", fees_maker)
                                .append("TickSize", tick_size)
                                .append("Status", status);

        mongoCollection.updateOne(eq("_id", wsName), Updates.push("Pairs", BSONPair));
    }
}
