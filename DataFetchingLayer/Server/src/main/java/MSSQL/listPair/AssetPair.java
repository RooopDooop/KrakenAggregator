package MSSQL.listPair;

import MSSQL.SQLConn;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class AssetPair {
    private final String AlternativeName;
    private final String WebsocketName;
    private Integer BaseAssetID;
    private Integer QuoteAssetID;
    private final Integer PairDecimals;
    private final Integer LotDecimals;
    private final Integer LotMultiplier;
    private Integer FeeAsset;
    private final Integer MarginCall;
    private final Integer MarginStop;
    private final BigDecimal OrderMinimum;

    public AssetPair(String insertAlternativeName, String insertWebsocketName, String insertBaseAsset, String insertQuoteAsset, Integer insertPairDecimals, Integer insertLotDecimals, Integer insertLotMultipler, String insertFeeAsset, Integer insertMarginCall, Integer insertMarginStop, BigDecimal insertOrderMinimum, Object rawFeesStandard, Object rawFeesMaker, Object rawLeverageBuy, Object rawLeverageSell) {
        this.AlternativeName = insertAlternativeName;
        this.WebsocketName = insertWebsocketName;

        try {
            this.BaseAssetID = new SQLConn().fetchAssetID(insertBaseAsset);
            this.QuoteAssetID = new SQLConn().fetchAssetID(insertQuoteAsset);

            if (insertFeeAsset.equals("ZUSD")) {
                this.FeeAsset = new SQLConn().fetchAssetID("USD");
            } else {
                throw new Exception("ERROR, invalid FeeCurrency");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        this.PairDecimals = insertPairDecimals;
        this.LotDecimals = insertLotDecimals;
        this.LotMultiplier = insertLotMultipler;
        this.MarginCall = insertMarginCall;
        this.MarginStop = insertMarginStop;
        this.OrderMinimum = insertOrderMinimum;

        List<Double> listLeverageBuy = (List<Double>)rawLeverageBuy;
        List<Double> listLeverageSell = (List<Double>)rawLeverageSell;

        //this.WritePairToDB();

        try {
            Integer PairID = new SQLConn().fetchPairID(this.AlternativeName);

            if (listLeverageBuy.size() > 0) {
                listLeverageBuy.forEach((intLeverage) -> {
                    //WriteLeverageToDB(PairID, intLeverage.intValue(), "Buy");
                });
            }

            if (listLeverageSell.size() > 0) {
                listLeverageSell.forEach((intLeverage) -> {
                    //WriteLeverageToDB(PairID, intLeverage.intValue(), "Sell");
                });
            }


            PairFees feesStandard = new PairFees(rawFeesStandard, "Standard", PairID);
            PairFees feesMaker = new PairFees(rawFeesMaker, "Maker", PairID);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void WritePairToDB() {
        try {
            new SQLConn().getSQL().createStatement().execute("EXEC [KrakenDB].[dbo].[insertPair] @AlternativePairName='" + this.AlternativeName + "', " +
                                                                                                                                                "@WebsocketPairName='" + this.WebsocketName + "', "+
                                                                                                                                                "@BaseID=" + this.BaseAssetID + ", " +
                                                                                                                                                "@QuoteID=" + this.QuoteAssetID + ", " +
                                                                                                                                                "@PairDecimals=" + this.PairDecimals + ", " +
                                                                                                                                                "@LotDecimals=" + this.LotDecimals + ", " +
                                                                                                                                                "@LotMultiplier=" + this.LotMultiplier + ", " +
                                                                                                                                                "@FeeCurrency=" + this.FeeAsset + ", " +
                                                                                                                                                "@MarginCall=" + this.MarginCall + ", " +
                                                                                                                                                "@MarginStop=" + this.MarginStop + ", " +
                                                                                                                                                "@OrderMinimum=" + this.OrderMinimum);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void WriteLeverageToDB(Integer PairID, Integer LeverageValue, String LeverageType) {
        try {
            new SQLConn().getSQL().createStatement().execute("EXEC [KrakenDB].[dbo].[insertLeverage] @PairID=" + PairID + ", " +
                    "@LeverageType='" + LeverageType + "', " +
                    "@LeverageValue=" + LeverageValue);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getAlternativeName() {
        return this.AlternativeName;
    }
    public String getWebsocketName() { return this.WebsocketName; }
    public Integer getBaseAssetID() { return this.BaseAssetID; }
    public Integer getQuoteAssetID() { return this.QuoteAssetID; }
    public Integer getPairDecimals() { return this.PairDecimals; }
    public Integer getLotDecimals() { return this.LotDecimals; }
    public Integer getLotMultiplier() { return this.LotMultiplier; }
    public Integer getFeeAsset() { return this.FeeAsset; }
    public Integer getMarginCall() { return this.MarginCall; }
    public Integer getMarginStop() { return this.MarginStop; }
    public BigDecimal getOrderMinimum() { return this.OrderMinimum; }
}