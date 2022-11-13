package MSSQL.Objects;

import java.math.BigDecimal;

public class sqlTrade {
    private int TradeID;
    private int PairID;
    private BigDecimal Price;
    private BigDecimal Volume;
    private BigDecimal tradeTime;
    private String Category;
    private String MarketOrLimit;

    public sqlTrade(int TradeID, int PairID, BigDecimal Price, BigDecimal Volume, BigDecimal tradeTime, String Category, String MarketOrLimit) {
        this.TradeID = TradeID;
        this.PairID = PairID;
        this.Price = Price;
        this.Volume = Volume;
        this.tradeTime = tradeTime;
        this.Category = Category;
        this.MarketOrLimit = MarketOrLimit;
    }
}
