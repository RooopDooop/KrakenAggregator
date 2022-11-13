package MSSQL.Objects;

import java.math.BigDecimal;

public class sqlFees {
    private int FeeID;
    private int PairID;
    private String FeeType;
    private int FeeVolume;
    private double FeePercentCost;

    public sqlFees(int FeeID, int PairID, String FeeType, int FeeVolume, double FeePercentCost) {
        this.FeeID = FeeID;
        this.PairID = PairID;
        this.FeeType = FeeType;
        this.FeeVolume = FeeVolume;
        this.FeePercentCost = FeePercentCost;
    }

    public int returnFeeID() {
        return this.FeeID;
    }

    public int returnPairID() {
        return this.PairID;
    }

    public String returnFeeType() {
        return this.FeeType;
    }

    public int returnFeeVolume() {
       return this.FeeVolume;
    }

    public double returnFeePercentCost() {
        return this.FeePercentCost;
    }
}
