USE KrakenDB
GO
/****** Object:  Table [dbo].[PairFeeInstances]    Script Date: 9/25/2022 4:06:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[PairFeeInstances](
	[FeeID] [int] IDENTITY(1,1) NOT NULL,
	[PairID] [int] NOT NULL,
	[FeeType] [varchar](10) NOT NULL,
	[FeeVolume] [int] NOT NULL,
	[FeePercentCost] [decimal](18, 2) NULL,
PRIMARY KEY CLUSTERED 
(
	[FeeID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]