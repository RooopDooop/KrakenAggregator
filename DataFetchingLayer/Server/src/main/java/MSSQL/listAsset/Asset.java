package MSSQL.listAsset;

import MSSQL.SQLConn;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class Asset {
    private final String Class;
    private final String AlternativeName;
    private final Integer Decimals;
    private final Integer Display_Decimals;
    private final BigDecimal Collateral;
    private final Boolean Fiat;

    public Asset(String insertClass, String insertAlt, Integer insertDecimal, Integer insertDisplay, BigDecimal insertCollateral) {
        this.Class = insertClass;
        this.AlternativeName = insertAlt;
        this.Decimals = insertDecimal;
        this.Display_Decimals = insertDisplay;
        this.Collateral = insertCollateral;

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
            this.Fiat = true;
        } else {
            this.Fiat = false;
        }

        this.pushAssetToDB();
    }

    private void pushAssetToDB() {
        try {
            new SQLConn().getSQL().createStatement().execute("EXEC [KrakenDB].[dbo].[insertAsset] @Class='" + this.Class + "', @AlternativeName='" + this.AlternativeName + "' , @Decimals=" + this.Decimals + ", @Display_Decimals=" + this.Display_Decimals + ", @Collateral=" + this.Collateral + ", @Fiat=" + this.Fiat);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}