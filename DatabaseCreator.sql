USE [KrakenDB]

/*-------------------------------------------------Tables-------------------------------------------------*/
CREATE TABLE Assets (
	AssetID int IDENTITY(1,1) PRIMARY KEY,
	Class varchar(50) NOT NULL,
	AlternativeName varchar(12) NOT NULL,
	Decimals int NOT NULL,
	DisplayDecimals int NOT NULL,
	CollateralValue decimal NULL,
	FiatAsset bit NOT NULL
)

CREATE TABLE AssetPairs (
	PairID int IDENTITY(1,1) PRIMARY KEY,
	AlternativePairName varchar(50) NOT NULL,
	WebsocketPairName varchar(50) NOT NULL,
	BaseID int NOT NULL FOREIGN KEY REFERENCES Assets(AssetID),
	QuoteID int NOT NULL FOREIGN KEY REFERENCES Assets(AssetID),
	PairDecimals int NOT NULL,
	LotDecimals int NOT NULL,
	LotMultiplier int NOT NULL,
	FeeCurrency varchar(12) NOT NULL,
	MarginCall int NOT NULL,
	MarginStop int NOT NULL,
	OrderMinimum varchar(50) NOT NULL
)

CREATE TABLE PairLeverageInstances (
	LeverageID int IDENTITY(1,1) PRIMARY KEY,
	PairID int NOT NULL,
	LeverageType varchar(5) NOT NULL,
	LeverageValue int NOT NULL
	FOREIGN KEY (PairID) REFERENCES AssetPairs(PairID)
)

CREATE TABLE PairFeeInstances (
	FeeID int IDENTITY(1,1) PRIMARY KEY,
	PairID int NOT NULL,
	FeeType varchar(10) NOT NULL,
	FeeVolume int NOT NULL,
	FeePercentCost decimal NOT NULL
	FOREIGN KEY (PairID) REFERENCES AssetPairs(PairID)
)

CREATE TABLE PairAssetTicker (
	TickerID int IDENTITY(1,1) PRIMARY KEY,
	TickerDate DATETIME NOT NULL,
	PairID int NOT NULL,
	AskingPrice decimal(38, 14) NOT NULL,
	AskingWholeLotVolume decimal(38, 14) NOT NULL,
	AskingLotVolume decimal(38, 14) NOT NULL,
	BidPrice decimal(38, 14) NOT NULL,
	BidWholeLotVolume decimal(38, 14) NOT NULL,
	BidLotVolume decimal(38, 14) NOT NULL,
	LastTradePrice decimal(38, 14) NOT NULL,
	LastTradeVolume decimal(38, 14) NOT NULL,
	VolumeToday decimal(38, 14) NOT NULL,
	VolumeLastTwentyFour decimal(38, 14) NOT NULL,
	VolumeWeightedToday decimal(38, 14) NOT NULL,
	VolumeWeightedLastTwentyFour decimal(38, 14) NOT NULL,
	TradeQuantity int NOT NULL,
	TradeQuantityLastTwentyFour int NOT NULL,
	LowToday decimal(38, 14) NOT NULL,
	LowLastTwentyFour decimal(38, 14) NOT NULL,
	HighToday decimal(38, 14) NOT NULL,
	HighLastTwentyFour decimal(38, 14) NOT NULL,
	OpeningPrice decimal(38, 14) NOT NULL,
	FOREIGN KEY (PairID) REFERENCES AssetPairs(PairID)
)

CREATE TABLE PairAssetOHLC (
	OHLCID int IDENTITY(1,1) PRIMARY KEY,
	retreivedTime DATETIME NOT NULL,
	PairID int,
	epoch int NOT NULL,
	priceOpen decimal(38, 14) NOT NULL,
	priceHigh decimal(38, 14) NOT NULL,
	priceLow decimal(38, 14) NOT NULL,
	priceClose decimal(38, 14) NOT NULL,
	priceVolumeWeightedAverage decimal(38, 14) NOT NULL,
	OHLCVolume decimal(38, 14) NOT NULL,
	OHLCCount int NOT NULL
	FOREIGN KEY (PairID) REFERENCES AssetPairs(PairID)
)

CREATE TABLE FiatConversionRates (
	ConversionID int IDENTITY(1,1) PRIMARY KEY,
	ConvDate DATETIME NOT NULL,
	ConvEpoch int NOT NULL,
	AssetFromID int,
	AssetToID int,
	ConvRate decimal(38, 14) NOT NULL
	FOREIGN KEY (AssetFromID) REFERENCES Assets(AssetID),
	FOREIGN KEY (AssetToID) REFERENCES Assets(AssetID)
)

CREATE TABLE AssetTrades (
	TradeID int IDENTITY(1,1) PRIMARY KEY,
	PairID int NOT NULL,
	Price decimal(38, 14) NOT NULL,
	Volume decimal(38, 14) NOT NULL,
	tradeTime decimal(38, 14) NOT NULL,
	Category varchar(16) NOT NULL,
	MarketOrLimit varchar(16) NOT NULL,
	FOREIGN KEY (PairID) REFERENCES AssetPairs(PairID)
)
/*-------------------------------------------------Tables-------------------------------------------------*/

/*-------------------------------------------------Stored proceedures-------------------------------------------------*/
CREATE PROCEDURE insertOHLC
	@JSONData VARCHAR(MAX)
AS
BEGIN
	SELECT GETDATE() AS RetreivedDate, *
	FROM OPENJSON(@JSONData)
	WITH (PairID int, epochTime int, priceOpen decimal(38, 14), priceHigh decimal(38, 14), priceLow decimal(38, 14), priceClose decimal(38, 14), VWAP decimal(38, 14), Volume decimal(38, 14), OHLCCount int) as insertValues
END
GO
/*-------------------------------------------------Stored proceedures-------------------------------------------------*/