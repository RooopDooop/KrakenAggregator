package MSSQL.Objects;

import java.math.BigDecimal;
import java.sql.Date;

public class sqlTicker {
    private int TickerID;
    private Date TickerDate;
    private int PairID;
    private BigDecimal AskingPrice;
    private BigDecimal AskingWholeLotVolume;
    private BigDecimal AskingLotVolume;
    private BigDecimal BidPrice;
    private BigDecimal BidWholeLotVolume;
    private BigDecimal BidLotVolume;
    private BigDecimal LastTradePrice;
    private BigDecimal LastTradeVolume;
    private BigDecimal VolumeToday;
    private BigDecimal VolumeLastTwentyFour;
    private BigDecimal VolumeWeightedToday;
    private BigDecimal VolumeWeightedLastTwentyFour;
    private int TradeQuantity;
    private int TradeQuantityLastTwentyFour;
    private BigDecimal LowToday;
    private BigDecimal LotLastTwentyFour;
    private BigDecimal HighToday;
    private BigDecimal HighLastTwentyFour;
    private BigDecimal OpeningPrice;

    public sqlTicker(int TickerID, Date TickerDate, int PairID, BigDecimal AskingPrice, BigDecimal AskingWholeLotVolume, BigDecimal AskingLotVolume, BigDecimal BidPrice, BigDecimal BidWholeLotVolume, BigDecimal BidLotVolume, BigDecimal LastTradePrice, BigDecimal LastTradeVolume, BigDecimal VolumeToday, BigDecimal VolumeLastTwentyFour, BigDecimal VolumeWeightedToday, BigDecimal VolumeWeightedLastTwentyFour, int TradeQuantity, int TradeQuantityLastTwentyFour, BigDecimal LowToday, BigDecimal LotLastTwentyFour, BigDecimal HighToday, BigDecimal HighLastTwentyFour, BigDecimal OpeningPrice) {
        this.TickerID = TickerID;
        this.TickerDate = TickerDate;
        this.PairID = PairID;
        this.AskingPrice = AskingPrice;
        this.AskingWholeLotVolume = AskingWholeLotVolume;
        this.AskingLotVolume = AskingLotVolume;
        this.BidPrice = BidPrice;
        this.BidWholeLotVolume = BidWholeLotVolume;
        this.BidLotVolume = BidLotVolume;
        this.LastTradePrice = LastTradePrice;
        this.LastTradeVolume = LastTradeVolume;
        this.VolumeToday = VolumeToday;
        this.VolumeLastTwentyFour = VolumeLastTwentyFour;
        this.VolumeWeightedToday = VolumeWeightedToday;
        this.VolumeWeightedLastTwentyFour = VolumeWeightedLastTwentyFour;
        this.TradeQuantity = TradeQuantity;
        this.TradeQuantityLastTwentyFour = TradeQuantityLastTwentyFour;
        this.LowToday = LowToday;
        this.LotLastTwentyFour = LotLastTwentyFour;
        this.HighToday = HighToday;
        this.HighLastTwentyFour = HighLastTwentyFour;
        this.OpeningPrice = OpeningPrice;
    }
}
