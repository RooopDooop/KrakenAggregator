package MSSQL.Objects;

import java.math.BigDecimal;
import java.sql.Date;

public class sqlConversionRate {
    private int ConversionID;
    private Date ConvDate;
    private int ConvEpoch;
    private int AssetFromID;
    private int AssetToID;
    private BigDecimal ConvRate;

    public sqlConversionRate() {

    }
}
