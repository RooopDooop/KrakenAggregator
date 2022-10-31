package MSSQL.Objects;

import java.math.BigDecimal;
import java.sql.Date;

public class sqlOHLC {
    private int OHLCID;
    private Date retreivedTime;
    private int PairID;
    private int epoch;
    private BigDecimal priceOpen;
    private BigDecimal priceHigh;
    private BigDecimal priceLow;
    private BigDecimal priceClose;
    private BigDecimal priceVolumeWeightedAverage;
    private BigDecimal OHLCVolume;
    private int OHLCCount;

    public sqlOHLC() {

    }
}
