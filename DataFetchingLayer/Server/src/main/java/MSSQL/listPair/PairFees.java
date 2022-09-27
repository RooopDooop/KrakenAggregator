package MSSQL.listPair;

import MSSQL.SQLConn;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class PairFees {
    Integer PairID;
    String Type;
    List<Object> rawFeeData;

    public PairFees(Object InputRawFeeData, String inputFeeType, Integer inputPairID) {
        try {
            this.Type = inputFeeType;
            this.PairID = inputPairID;
            this.rawFeeData = (List<Object>)InputRawFeeData;

            rawFeeData.forEach((Fee) -> {
                Integer Volume = ((Double)((List<Object>)Fee).get(0)).intValue();
                Double Percentage = (Double)((List<Object>)Fee).get(1);
                WriteFeeToDB(Volume, Percentage);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void WriteFeeToDB(Integer Volume, Double Percentage) {
        try {
            new SQLConn().getSQL().createStatement().execute("EXEC [KrakenDB].[dbo].[insertFee] @PairID='" + this.PairID + "', " +
                    "@FeeType='" + this.Type + "', " +
                    "@FeeVolume=" + Volume + ", " +
                    "@FeePercentage=" + Percentage);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
