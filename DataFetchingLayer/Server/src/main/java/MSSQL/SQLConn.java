package MSSQL;

import MSSQL.AssetTask.Asset;
import MSSQL.PairTask.Fee;
import org.javatuples.Octet;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SQLConn {
    private static Connection SQLConnection;

    //This is a singleton class
    public static Connection getSQL() {
        if (SQLConnection == null) {
            try  {
                SQLConnection = DriverManager.getConnection("jdbc:sqlserver://localhost:1433; database=KrakenDB; user=sa; password=REMOVED; encrypt=false; trustServerCertificate=false; loginTimeout=30;");
                System.out.println("Connected to DB!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return SQLConnection;
    }

    public int fetchAssetID(String AlternativeName) throws SQLException {
        ResultSet resultSet = getSQL().createStatement().executeQuery("EXEC new_selectAssetID @AlternativeName = '" + AlternativeName + "'");
        resultSet.next();
        return resultSet.getInt("AssetID");
    }

    public ArrayList<Asset> fetchAllAssets() throws SQLException {
        ArrayList<Asset> listSQLAssets = new ArrayList<>();
        ResultSet resultSet = getSQL().createStatement().executeQuery(" EXEC dbo.new_fetchAssets");

        while(resultSet.next()) {
            Asset objAsset = new Asset(
                    resultSet.getString("AlternativeName"),
                    resultSet.getString("Class"),
                    resultSet.getDouble("Decimals"),
                    resultSet.getDouble("DisplayDecimals"),
                    resultSet.getBigDecimal("CollateralValue")
            );

            listSQLAssets.add(objAsset);
        }

        return listSQLAssets;
    }

    public int fetchPairID(String AlternativePairName) throws SQLException {
        ResultSet resultSet = getSQL().createStatement().executeQuery("EXEC new_selectPairID @AlternativeName = '" + AlternativePairName + "'");
        resultSet.next();
        return resultSet.getInt("PairID");
    }

    public int insertWSMessage(String strType, String strMessage, String strHost, String strClient) throws SQLException {
        ResultSet resultSet = getSQL().createStatement().executeQuery("EXEC insertWSMessage @Type = '" + strType + "', @Message = '" + strMessage + "', @HostAddress = '" + strHost + "', @ClientAddress='" + strClient + "'");
        resultSet.next();
        return resultSet.getInt("MessageID");
    }

    public Map<String, Octet<String, Integer, Integer, Integer, Integer, Integer, Integer, BigDecimal>>  fetchPairs() throws SQLException {
        Map<String, Octet<String, Integer, Integer, Integer, Integer, Integer, Integer, BigDecimal>> mapTupleValues = new HashMap<>();
        ResultSet resultSet = getSQL().createStatement().executeQuery("EXEC [dbo].[fetchPairs]");

        while(resultSet.next()) {
            Octet<String, Integer, Integer, Integer, Integer, Integer, Integer, BigDecimal> tupleValues = new Octet<>(
                    resultSet.getString("AlternativePairName"),
                    resultSet.getInt("PairID"),
                    resultSet.getInt("PairDecimals"),
                    resultSet.getInt("LotDecimals"),
                    resultSet.getInt("LotMultiplier"),
                    resultSet.getInt("MarginCall"),
                    resultSet.getInt("MarginStop"),
                    resultSet.getBigDecimal("OrderMinimum")
            );

            mapTupleValues.put(resultSet.getString("AlternativePairName"), tupleValues);
        }

        return mapTupleValues;
    }

    public Map<Integer, Fee> fetchFees() throws SQLException {
        Map<Integer, Fee> mapFees = new HashMap<>();
        ResultSet resultSet = getSQL().createStatement().executeQuery("EXEC [dbo].[new_fetchFees]");

        while(resultSet.next()) {
            Fee tupleValues = new Fee(
                    resultSet.getInt("PairID"),
                    resultSet.getString("FeeType"),
                    resultSet.getInt("FeeVolume"),
                    resultSet.getBigDecimal("FeePercentCost")
            );

            mapFees.put(resultSet.getInt("FeeID"), tupleValues);
        }

        return mapFees;
    }

    public void insertAssets(String JSONData) throws SQLException {
        System.out.println("Inserting Assets");
        getSQL().createStatement().execute("EXEC new_insertAsset @JSONData = '" + JSONData + "'");
    }

    public void updateAssets(String JSONData) throws SQLException {
        System.out.println("Updating Assets");
        getSQL().createStatement().execute("EXEC new_updateAsset @JSONData = '" + JSONData + "'");
    }

    public void insertPairs(String JSONData) throws SQLException {
        System.out.println("Inserting Pairs");
        getSQL().createStatement().execute("EXEC insertPair @JSONData = '" + JSONData + "'");
    }

    public void updatePairs(String JSONData) throws SQLException {
        System.out.println("Updating Pairs");
        getSQL().createStatement().execute("EXEC updatePair @JSONData = '" + JSONData + "'");
    }

    public void insertFees(String JSONData) throws SQLException {
        System.out.println("Inserting Fees");
        getSQL().createStatement().execute("EXEC new_insertFees @JSONData = '" + JSONData + "'");
    }

    public void updateFees(String JSONData) throws SQLException {
        System.out.println("Updating Fees");
        getSQL().createStatement().execute("EXEC new_updateFee @JSONData = '" + JSONData + "'");
    }

    public void insertLeverages(String JSONData, String LeverageType) throws SQLException {
        System.out.println("Inserting " + LeverageType + " Leverages");
        getSQL().createStatement().execute("EXEC new_insertLeverages @JSONData = '" + JSONData + "', @LeverageType ='" + LeverageType + "'");
    }
}
