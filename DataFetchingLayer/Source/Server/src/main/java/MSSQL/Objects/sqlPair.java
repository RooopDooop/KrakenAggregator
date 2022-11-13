package MSSQL.Objects;

import java.math.BigDecimal;

public class sqlPair {
    private int PairID;
    private String AlternativePairName;
    private String WebsocketPairName;
    private int BaseID;
    private int QuoteID;
    private int PairDecimals;
    private int LotDecimals;
    private int LotMultiplier;
    private int FeeCurrency;
    private int MarginCall;
    private int MarginStop;
    private BigDecimal OrderMinimum;
    private BigDecimal CostMinimum;

    public sqlPair(int PairID, String AlternativePairName, String WebsocketPairName, int BaseID, int QuoteID, int PairDecimals, int LotDecimals, int LotMultiplier, int FeeCurrency, int MarginCall, int MarginStop, BigDecimal OrderMinimum, BigDecimal CostMinimum) {
        this.PairID = PairID;
        this.AlternativePairName = AlternativePairName;
        this.WebsocketPairName = WebsocketPairName;
        this.BaseID = BaseID;
        this.QuoteID = QuoteID;
        this.PairDecimals = PairDecimals;
        this.LotDecimals = LotDecimals;
        this.LotMultiplier = LotMultiplier;
        this.FeeCurrency = FeeCurrency;
        this.MarginCall = MarginCall;
        this.MarginStop = MarginStop;
        this.OrderMinimum = OrderMinimum;

        if (CostMinimum == null) {
            this.CostMinimum = new BigDecimal(0);
        } else {
            this.CostMinimum = CostMinimum;
        }
    }

    public int GetPairID() {
        return this.PairID;
    }

    public String GetAlternativeName() {
        return this.AlternativePairName;
    }
    public int returnPairDecimals() { return this.PairDecimals; }
    public int returnLotDecimals() { return this.LotDecimals; }
    public int returnLotMultipliers() { return this.LotMultiplier; }
    public int returnMarginCall() { return this.MarginCall; }
    public int returnMarginStop() { return this.MarginStop; }
    public BigDecimal returnOrderMinimum() { return this.OrderMinimum; }
    public BigDecimal returnCostMinimum() { return this.CostMinimum; }
}
