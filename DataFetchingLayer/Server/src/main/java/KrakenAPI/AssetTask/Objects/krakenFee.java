package KrakenAPI.AssetTask.Objects;

import java.math.BigDecimal;

public class krakenFee {
    private final int PairID;
    private final String FeeType;
    private final int Volume;
    private final double PercentCost;

    public krakenFee(int PairID, String FeeType, int Volume, double PercentCost) {
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

    public double GetPercentCost() {
        return PercentCost;
    }
}
