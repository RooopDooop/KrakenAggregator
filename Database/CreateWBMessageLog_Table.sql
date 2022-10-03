USE KrakenDB
GO
/****** Object:  Table [dbo].[WBMessageLog]    Script Date: 9/25/2022 4:06:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[WBMessageLog](
	[MessageID] [int] IDENTITY(1,1) NOT NULL,
	[Type] [varchar](50) NOT NULL,
	[Message] [varchar](2048) NOT NULL,
	[Time] [datetime] NOT NULL,
	[HostAddress] [varchar](50) NOT NULL,
	[ClientAddress] [varchar](50) NOT NULL
) ON [PRIMARY]