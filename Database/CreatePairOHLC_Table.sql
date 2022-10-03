USE KrakenDB
GO
/****** Object:  Table [dbo].[PairAssetOHLC]    Script Date: 9/25/2022 4:06:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[PairOHLC](
	[OHLCID] [int] IDENTITY(1,1) NOT NULL,
	[retreivedTime] [datetime] NOT NULL,
	[PairID] [int] NULL,
	[epoch] [int] NOT NULL,
	[priceOpen] [decimal](38, 14) NOT NULL,
	[priceHigh] [decimal](38, 14) NOT NULL,
	[priceLow] [decimal](38, 14) NOT NULL,
	[priceClose] [decimal](38, 14) NOT NULL,
	[priceVolumeWeightedAverage] [decimal](38, 14) NOT NULL,
	[OHLCVolume] [decimal](38, 14) NOT NULL,
	[OHLCCount] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[OHLCID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]