USE KrakenDB
GO
/****** Object:  Table [dbo].[PairLeverageInstances]    Script Date: 9/25/2022 4:06:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[PairLeverageInstances](
	[LeverageID] [int] IDENTITY(1,1) NOT NULL,
	[PairID] [int] NOT NULL,
	[LeverageType] [varchar](5) NOT NULL,
	[LeverageValue] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[LeverageID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]