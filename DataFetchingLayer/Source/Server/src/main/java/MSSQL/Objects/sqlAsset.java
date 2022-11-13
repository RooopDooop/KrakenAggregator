package MSSQL.Objects;

import java.math.BigDecimal;

public class sqlAsset {
    private int AssetID;
    private String strClass;
    private String AlternativeName;
    private int Decimal;
    private int DisplayDecimals;
    private BigDecimal CollateralValue;
    private boolean Fiat;

    public sqlAsset(int AssetID, String strClass, String AlternativeName, int Decimal, int DisplayDecimals, BigDecimal CollateralValue, boolean Fiat) {
        this.AssetID = AssetID;
        this.strClass = strClass;
        this.AlternativeName = AlternativeName;
        this.Decimal = Decimal;
        this.DisplayDecimals = DisplayDecimals;
        this.CollateralValue = CollateralValue;
        this.Fiat = Fiat;
    }

    public int GetAssetID() { return this.AssetID; }

    public String GetStrClass() {
        return this.strClass;
    }

    public String GetAlternativeName() {
        return this.AlternativeName;
    }

    public int GetDecimals() {
        return this.Decimal;
    }

    public int GetDisplayDecimals() {
        return this.DisplayDecimals;
    }

    public BigDecimal GetCollateralValue() {
        return this.CollateralValue;
    }

    public boolean GetIsFiat() { return this.Fiat; }
}
