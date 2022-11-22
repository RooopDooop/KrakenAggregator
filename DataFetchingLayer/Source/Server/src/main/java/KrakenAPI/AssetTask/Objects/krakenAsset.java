package KrakenAPI.AssetTask.Objects;

import Mongo.MongoConn;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.eq;

public class krakenAsset {
    private final String AlternativeName;
    private final String Class;
    private final int Decimals;
    private final int DisplayDecimals;
    private final BigDecimal CollateralValue;
    private final String Status;
    private final boolean isFiat;

    public krakenAsset(String AlternativeName, String Class, double Decimals, double DisplayDecimals, BigDecimal CollateralValue, String status) {
        this.AlternativeName = AlternativeName;
        this.Class = Class;
        this.Decimals = (int)Decimals;
        this.DisplayDecimals = (int)DisplayDecimals;
        this.Status = status;
        this.CollateralValue = Objects.requireNonNullElseGet(CollateralValue, () -> new BigDecimal(0.0));
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

        return fiatAssets.contains(this.AlternativeName);
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

    public void WriteToMongo() {
        MongoCollection mongoCollection = MongoConn.getMongo().getDatabase("KrakenDB").getCollection("Assets");

        Document BSONAsset = new Document("_id", AlternativeName)
                                .append("Class", Class)
                                .append("Decimals", Decimals)
                                .append("DisplayDecimals", DisplayDecimals)
                                .append("CollateralValue", CollateralValue)
                                .append("isFiat", isFiat)
                                .append("Status", Status);

        FindIterable mongoIterator = mongoCollection.find(eq("_id", AlternativeName));

        if (mongoIterator.first() == null) {
            mongoCollection.insertOne(BSONAsset);
        } else {
            mongoCollection.replaceOne(Filters.eq("_id", AlternativeName), BSONAsset);
        }
    }
}
