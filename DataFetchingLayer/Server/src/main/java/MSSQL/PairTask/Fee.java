package MSSQL.PairTask;

import java.math.BigDecimal;

public class Fee {
    private final int PairID;
    private final String FeeType;
    private final int Volume;
    private final BigDecimal PercentCost;

    public Fee(int PairID, String FeeType, int Volume, BigDecimal PercentCost) {
        this.PairID = PairID;
        this.FeeType = FeeType;
        this.Volume = Volume;
        this.PercentCost = PercentCost;
    }

    public int GetPairID() {
        return this.PairID;
    }

    public String GetFeeType() {
        return this.FeeType;
    }

    public int GetVolume() {
        return Volume;
    }

    public BigDecimal GetPercentCost() {
        return PercentCost;
    }
}
