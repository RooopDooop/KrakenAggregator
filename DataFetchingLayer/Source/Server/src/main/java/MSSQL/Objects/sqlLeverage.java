package MSSQL.Objects;

public class sqlLeverage {
    private int LeverageID;
    private int PairID;
    private String LeverageType;
    private int LeverageValue;

    public sqlLeverage(int LeverageID, int PairID, String LeverageType, int LeverageValue) {
        this.LeverageID = LeverageID;
        this.PairID = PairID;
        this.LeverageType = LeverageType;
        this.LeverageValue = LeverageValue;
    }
}
