USE KrakenDB
GO
/****** Object:  Table [dbo].[FiatConversionRates]    Script Date: 9/25/2022 4:06:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[FiatConversionRates](
	[ConversionID] [int] IDENTITY(1,1) NOT NULL,
	[ConvDate] [datetime] NOT NULL,
	[ConvEpoch] [int] NOT NULL,
	[AssetFromID] [int] NULL,
	[AssetToID] [int] NULL,
	[ConvRate] [decimal](38, 14) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ConversionID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]