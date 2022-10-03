USE KrakenDB
GO
/****** Object:  Table [dbo].[PairAssetTicker]    Script Date: 9/25/2022 4:06:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[PairTickers](
	[TickerID] [int] IDENTITY(1,1) NOT NULL,
	[TickerDate] [datetime] NOT NULL,
	[PairID] [int] NOT NULL,
	[AskingPrice] [decimal](38, 14) NOT NULL,
	[AskingWholeLotVolume] [decimal](38, 14) NOT NULL,
	[AskingLotVolume] [decimal](38, 14) NOT NULL,
	[BidPrice] [decimal](38, 14) NOT NULL,
	[BidWholeLotVolume] [decimal](38, 14) NOT NULL,
	[BidLotVolume] [decimal](38, 14) NOT NULL,
	[LastTradePrice] [decimal](38, 14) NOT NULL,
	[LastTradeVolume] [decimal](38, 14) NOT NULL,
	[VolumeToday] [decimal](38, 14) NOT NULL,
	[VolumeLastTwentyFour] [decimal](38, 14) NOT NULL,
	[VolumeWeightedToday] [decimal](38, 14) NOT NULL,
	[VolumeWeightedLastTwentyFour] [decimal](38, 14) NOT NULL,
	[TradeQuantity] [int] NOT NULL,
	[TradeQuantityLastTwentyFour] [int] NOT NULL,
	[LowToday] [decimal](38, 14) NOT NULL,
	[LowLastTwentyFour] [decimal](38, 14) NOT NULL,
	[HighToday] [decimal](38, 14) NOT NULL,
	[HighLastTwentyFour] [decimal](38, 14) NOT NULL,
	[OpeningPrice] [decimal](38, 14) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[TickerID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]