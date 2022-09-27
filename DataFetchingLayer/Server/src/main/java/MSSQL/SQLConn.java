package MSSQL;

import org.javatuples.Octet;
import org.javatuples.Septet;

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
                SQLConnection = DriverManager.getConnection("jdbc:sqlserver://192.168.0.20:1433; database=KrakenDB; user=sa; password=REMOVED; encrypt=false; trustServerCertificate=false; loginTimeout=30;");
                System.out.println("Connected to DB!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return SQLConnection;
    }

    public int fetchAssetID(String AlternativeName) throws SQLException {
        ResultSet resultSet = getSQL().createStatement().executeQuery("EXEC selectAssetID @AlternativeName = '" + AlternativeName + "'");
        resultSet.next();
        return resultSet.getInt("AssetID");
    }

    public Map<String, Integer> fetchAllAssets() throws SQLException {
        Map<String, Integer> mapTupleValues = new HashMap<>();
        ResultSet resultSet = getSQL().createStatement().executeQuery(" EXEC dbo.fetchAssets");

        while(resultSet.next()) {
            mapTupleValues.put(resultSet.getString("AlternativeName"), resultSet.getInt("AssetID"));
        }

        return mapTupleValues;
    }

    public int fetchPairID(String AlternativePairName) throws SQLException {
        ResultSet resultSet = getSQL().createStatement().executeQuery("EXEC selectPairID @AlternativeName = '" + AlternativePairName + "'");
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
}
