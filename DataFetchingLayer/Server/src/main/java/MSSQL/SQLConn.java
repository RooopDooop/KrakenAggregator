package MSSQL;

import MSSQL.Objects.sqlAsset;
import MSSQL.Objects.sqlFees;
import MSSQL.Objects.sqlPair;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SQLConn {
    private static Connection SQLConnection;

    //This is a singleton class
    public static Connection getSQL() {
        if (SQLConnection == null) {
            try  {
                SQLConnection = DriverManager.getConnection("jdbc:sqlserver://192.168.1.120:1433; database=KrakenDB; user=sa; password=REMOVED; encrypt=false; trustServerCertificate=false; loginTimeout=30;");
                System.out.println("Connected to DB!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return SQLConnection;
    }

    public Map<String, sqlAsset> compareAllAssets(String arrAlternativeNames) throws SQLException {
        ResultSet resultSet = getSQL().createStatement().executeQuery("EXEC GET_CompareAllAssets @AggregatedAlternatives = '" + arrAlternativeNames + "'");
        Map<String, sqlAsset> returnValues = new HashMap<>();

        while(resultSet.next()) {
            sqlAsset objAsset = new sqlAsset(
                    resultSet.getInt("AssetID"),
                    resultSet.getString("Class"),
                    resultSet.getString("AlternativeName"),
                    resultSet.getInt("Decimals"),
                    resultSet.getInt("DisplayDecimals"),
                    resultSet.getBigDecimal("CollateralValue"),
                    resultSet.getBoolean("FiatAsset")
            );

            returnValues.put(objAsset.GetAlternativeName(), objAsset);
        };

        return returnValues;
    }

    public Map<String, sqlPair> compareAllPairs(String arrAlternativeNames) throws SQLException {
        ResultSet resultSet = getSQL().createStatement().executeQuery("EXEC GET_CompareAllPairs @AggregatedAlternatives = '" + arrAlternativeNames + "'");
        Map<String, sqlPair> returnValues = new HashMap<>();

        while(resultSet.next()) {
            sqlPair objPair = new sqlPair(
                    resultSet.getInt("PairID"),
                    resultSet.getString("AlternativeName"),
                    resultSet.getString("WebsocketPairName"),
                    resultSet.getInt("BaseID"),
                    resultSet.getInt("QuoteID"),
                    resultSet.getInt("PairDecimals"),
                    resultSet.getInt("LotDecimals"),
                    resultSet.getInt("LotMultiplier"),
                    resultSet.getInt("FeeCurrency"),
                    resultSet.getInt("MarginCall"),
                    resultSet.getInt("MarginStop"),
                    resultSet.getBigDecimal("OrderMinimum"),
                    resultSet.getBigDecimal("CostMinimum")
            );

            returnValues.put(objPair.GetAlternativeName(), objPair);
        };

        return returnValues;
    }

    public void insertBulkAssets(String JSONData) throws SQLException {
        getSQL().createStatement().execute("EXEC PUT_InsertAssets @JSONData = '" + JSONData + "'");
    }

    public void updateBulkAssets(String JSONData) throws SQLException {
        getSQL().createStatement().execute("EXEC PATCH_UpdateAssets @JSONData = '" + JSONData + "'");
    }

    public void insertBulkPairs(String JSONData) throws SQLException {
        getSQL().createStatement().execute("EXEC PUT_InsertPairs @JSONData = '" + JSONData + "'");
    }

    public void updateBulkPairs(String JSONData) throws SQLException {
        getSQL().createStatement().execute("EXEC PATCH_UpdatePairs @JSONData = '" + JSONData + "'");
    }

    public void insertBulkFees(String JSONData) throws SQLException {
        getSQL().createStatement().execute("EXEC PUT_InsertFees @JSONData = '" + JSONData + "'");
    }

    public void updateBulkFees(String JSONData) throws SQLException {
        getSQL().createStatement().execute("EXEC PATCH_UpdateFees @JSONData = '" + JSONData + "'");
    }

    public void insertBulkLeverages(String JSONData) throws SQLException {
        getSQL().createStatement().execute("EXEC PUT_InsertLeverages @JSONData = '" + JSONData + "'");
    }

    public void deleteBulkLeverages(String JSONData) throws SQLException {
        getSQL().createStatement().execute("EXEC DELETE_TrimLeverages @JSONData = '" + JSONData + "'");
    }

    public Map<String, Integer> fetchAllAssetIDs() throws SQLException {
        Map<String, Integer> returnMap = new HashMap<>();
        ResultSet resultSet = getSQL().createStatement().executeQuery("EXEC dbo.GET_ReturnAllAssetIDs");

        while(resultSet.next()) {
            returnMap.put(resultSet.getString("AlternativeName"), resultSet.getInt("AssetID"));
        }

        return returnMap;
    }

    public Map<Integer, sqlFees> fetchFees() throws SQLException {
        Map<Integer, sqlFees> mapFees = new HashMap<>();
        ResultSet resultSet = getSQL().createStatement().executeQuery("EXEC [dbo].[GET_ReturnFees]");

        while(resultSet.next()) {
            sqlFees objFee = new sqlFees(
                    resultSet.getInt("FeeID"),
                    resultSet.getInt("PairID"),
                    resultSet.getString("FeeType"),
                    resultSet.getInt("FeeVolume"),
                    resultSet.getDouble("FeePercentCost")
            );

            mapFees.put(resultSet.getInt("FeeID"), objFee);
        }

        return mapFees;
    }
}
