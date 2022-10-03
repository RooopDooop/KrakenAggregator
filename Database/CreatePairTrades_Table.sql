USE KrakenDB
GO
/****** Object:  Table [dbo].[AssetTrades]    Script Date: 9/25/2022 4:06:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[PairTrades](
	[TradeID] [int] IDENTITY(1,1) NOT NULL,
	[PairID] [int] NOT NULL,
	[Price] [decimal](38, 14) NOT NULL,
	[Volume] [decimal](38, 14) NOT NULL,
	[tradeTime] [decimal](38, 14) NOT NULL,
	[Category] [varchar](16) NOT NULL,
	[MarketOrLimit] [varchar](16) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[TradeID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]