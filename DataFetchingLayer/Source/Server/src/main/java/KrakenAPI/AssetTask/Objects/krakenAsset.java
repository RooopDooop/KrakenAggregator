package KrakenAPI.AssetTask.Objects;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class krakenAsset {
    private final String AlternativeName;
    private final String Class;
    private final int Decimals;
    private final int DisplayDecimals;
    private final BigDecimal CollateralValue;
    private final boolean isFiat;

    public krakenAsset(String AlternativeName, String Class, double Decimals, double DisplayDecimals, BigDecimal CollateralValue) {
        this.AlternativeName = AlternativeName;
        this.Class = Class;
        this.Decimals = (int)Decimals;
        this.DisplayDecimals = (int)DisplayDecimals;

        if (CollateralValue != null) {
            this.CollateralValue = CollateralValue;
        } else {
            this.CollateralValue = new BigDecimal(0.0);
        }

        this.isFiat = determineFiat();
    }

    private boolean determineFiat() {
        Set<String> fiatAssets = new HashSet<String>();
        fiatAssets.add("GBP");
        fiatAssets.add("GBP.HOLD");
        fiatAssets.add("AUD");
        fiatAssets.add("AUD.HOLD");
        fiatAssets.add("EUR");
        fiatAssets.add("EUR.HOLD");
        fiatAssets.add("CAD");
        fiatAssets.add("CAD.HOLD");
        fiatAssets.add("USD");
        fiatAssets.add("USD.HOLD");
        fiatAssets.add("CHF");
        fiatAssets.add("CHF.HOLD");
        fiatAssets.add("JPY");

        if (fiatAssets.contains(this.AlternativeName)) {
            return true;
        }

        return false;
    }

    public String GetAlternativeName() {
        return this.AlternativeName;
    }

    public String GetClass() {
        return this.Class;
    }

    public int GetDecimals() {
        return this.Decimals;
    }

    public int GetDisplayDecimals() {
        return this.DisplayDecimals;
    }

    public BigDecimal GetCollateralValue() {
        return this.CollateralValue;
    }

    public boolean GetIsFiat() {
        return this.isFiat;
    }
}
