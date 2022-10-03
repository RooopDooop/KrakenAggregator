USE KrakenDB
GO
/****** Object:  Table [dbo].[Assets]    Script Date: 9/25/2022 4:06:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Assets](
	[AssetID] [int] IDENTITY(1,1) NOT NULL,
	[Class] [varchar](50) NOT NULL,
	[AlternativeName] [varchar](12) NOT NULL,
	[Decimals] [int] NOT NULL,
	[DisplayDecimals] [int] NOT NULL,
	[CollateralValue] [decimal](18, 1) NOT NULL,
	[FiatAsset] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[AssetID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]