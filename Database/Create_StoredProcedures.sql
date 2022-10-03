USE KrakenDB
GO
/****** Object:  StoredProcedure [dbo].[fetchAssets]    Script Date: 9/25/2022 4:06:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[fetchAssets]

AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for procedure here
	SELECT dbo.Assets.AlternativeName, dbo.Assets.AssetID
	FROM dbo.Assets
END
GO
/****** Object:  StoredProcedure [dbo].[fetchPairs]    Script Date: 9/25/2022 4:06:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[fetchPairs]

AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for procedure here
	SELECT PairID, AlternativePairName, PairDecimals, LotDecimals, LotMultiplier, MarginCall, MarginStop, OrderMinimum
	FROM dbo.AssetPairs
END
GO
/****** Object:  StoredProcedure [dbo].[insertAsset]    Script Date: 9/25/2022 4:06:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[insertAsset] 
	@Class varchar(50), 
	@AlternativeName varchar(12), 
	@Decimals int, 
	@Display_Decimals int, 
	@Collateral decimal(18, 1), 
	@Fiat bit
AS
BEGIN
	SET NOCOUNT ON;

	IF EXISTS (SELECT dbo.Assets.AssetID FROM dbo.Assets WHERE dbo.Assets.AlternativeName = @AlternativeName)
		BEGIN
			UPDATE dbo.Assets
			SET Class = @Class,
				Decimals = @Decimals,
				DisplayDecimals = @Display_Decimals,
				CollateralValue = @Collateral,
				FiatAsset = @Fiat
			WHERE dbo.Assets.AlternativeName = @AlternativeName
		END
	ELSE
		BEGIN 
			INSERT INTO dbo.Assets(Class, AlternativeName, Decimals, DisplayDecimals, CollateralValue, FiatAsset) 
			VALUES(@Class, @AlternativeName, @Decimals, @Display_Decimals, @Collateral, @Fiat)
		END
END
GO
/****** Object:  StoredProcedure [dbo].[insertFee]    Script Date: 9/25/2022 4:06:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[insertFee] 
	@PairID Int, 
	@FeeType Varchar(10),
	@FeeVolume Int,
	@FeePercentage decimal(18, 2)
AS
BEGIN
	SET NOCOUNT ON;

	IF NOT EXISTS (SELECT dbo.PairFeeInstances.FeeID FROM dbo.PairFeeInstances WHERE dbo.PairFeeInstances.PairID = @PairID AND dbo.PairFeeInstances.FeeType = @FeeType AND dbo.PairFeeInstances.FeeVolume = @FeeVolume AND dbo.PairFeeInstances.FeePercentCost = @FeePercentage)
		BEGIN
			INSERT INTO dbo.PairFeeInstances(PairID, FeeType, FeeVolume, FeePercentCost) 
			VALUES(@PairID, @FeeType, @FeeVolume, @FeePercentage)
		END
END
GO
/****** Object:  StoredProcedure [dbo].[insertFiatConversion]    Script Date: 9/25/2022 4:06:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[insertFiatConversion]
	 @convEpoch int, 
	 @convFromID int, 
	 @convToID int, 
	 @convRate decimal(38, 14)
AS
BEGIN
	INSERT INTO [KrakenDB].[dbo].[FiatConversionRates] (ConvDate, ConvEpoch, AssetFromID, AssetToID, ConvRate)
	VALUES (GETDATE(), @convEpoch, @convFromID, @convToID, @convRate); 
END
GO
/****** Object:  StoredProcedure [dbo].[insertLeverage]    Script Date: 9/25/2022 4:06:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[insertLeverage] 
	@PairID Int, 
	@LeverageType Varchar(10),
	@LeverageValue Int
AS
BEGIN
	SET NOCOUNT ON;

	IF NOT EXISTS (SELECT dbo.PairLeverageInstances.LeverageID FROM dbo.PairLeverageInstances WHERE dbo.PairLeverageInstances.PairID = @PairID AND dbo.PairLeverageInstances.LeverageType = @LeverageType AND dbo.PairLeverageInstances.LeverageValue = @LeverageValue)
		BEGIN
			INSERT INTO dbo.PairLeverageInstances(PairID, LeverageType, LeverageValue) 
			VALUES(@PairID, @LeverageType, @LeverageValue)
		END
END
GO
/****** Object:  StoredProcedure [dbo].[insertOHLC]    Script Date: 9/25/2022 4:06:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[insertOHLC]
	@JSONData VARCHAR(MAX)
AS
BEGIN
	INSERT INTO dbo.PairAssetOHLC
	SELECT GETDATE(), *
	FROM OPENJSON(@JSONData)
	WITH (PairID int, epochTime int, priceOpen decimal(38, 14), priceHigh decimal(38, 14), priceLow decimal(38, 14), priceClose decimal(38, 14), VWAP decimal(38, 14), volume decimal(38, 14), [count] int) as insertValues
	WHERE NOT EXISTS (SELECT PairID, epoch, priceOpen, priceHigh, priceLow, priceClose, dbo.PairAssetOHLC.priceVolumeWeightedAverage, dbo.PairAssetOHLC.OHLCVolume, dbo.PairAssetOHLC.OHLCCount 
		FROM dbo.PairAssetOHLC
		WHERE PairID = insertValues.PairID
		AND epoch = insertValues.epochTime
		AND priceOpen = insertValues.priceOpen
		AND priceHigh = insertValues.priceHigh
		AND priceLow = insertValues.priceLow
		AND priceClose = insertValues.priceClose
		AND dbo.PairAssetOHLC.priceVolumeWeightedAverage = insertValues.VWAP
		AND dbo.PairAssetOHLC.OHLCVolume = insertValues.volume
		AND dbo.PairAssetOHLC.OHLCCount = insertValues.[count])
END
GO
/****** Object:  StoredProcedure [dbo].[insertPair]    Script Date: 9/25/2022 4:06:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[insertPair] 
	@AlternativePairName	varchar(50),
	@WebsocketPairName	varchar(50),
	@BaseID	int,
	@QuoteID	int,
	@PairDecimals	int,
	@LotDecimals	int,
	@LotMultiplier	int,
	@FeeCurrency	int,
	@MarginCall	int,
	@MarginStop	int,
	@OrderMinimum	decimal(38, 8)
AS
BEGIN
	SET NOCOUNT ON;

	INSERT INTO dbo.AssetPairs(AlternativePairName, WebsocketPairName, BaseID, QuoteID, PairDecimals, LotDecimals, LotMultiplier, FeeCurrency, MarginCall, MarginStop, OrderMinimum) 
	VALUES(@AlternativePairName, @WebsocketPairName, @BaseID, @QuoteID, @PairDecimals, @LotDecimals, @LotMultiplier, @FeeCurrency, @MarginCall, @MarginStop, @OrderMinimum)
END
GO
/****** Object:  StoredProcedure [dbo].[insertWSMessage]    Script Date: 9/25/2022 4:06:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[insertWSMessage] 
	@Type VARCHAR(50), 
	@Message VARCHAR(2048),
	@HostAddress VARCHAR(50),
	@ClientAddress  VARCHAR(50)
AS
BEGIN
	SET NOCOUNT ON;

	INSERT INTO dbo.WBMessageLog([Type], [Message], [Time], HostAddress, ClientAddress) 
	OUTPUT INSERTED.MessageID
	VALUES(@Type, @Message, GETDATE(), @HostAddress, @ClientAddress)
END
GO
/****** Object:  StoredProcedure [dbo].[selectAssetID]    Script Date: 9/25/2022 4:06:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[selectAssetID]
	@AlternativeName VARCHAR(16)
AS
BEGIN
	SET NOCOUNT ON;
	SELECT AssetID FROM KrakenDB.dbo.Assets WHERE dbo.Assets.AlternativeName = @AlternativeName
END
GO
/****** Object:  StoredProcedure [dbo].[selectPairID]    Script Date: 9/25/2022 4:06:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[selectPairID]
	@AlternativeName VARCHAR(16)
AS
BEGIN
	SET NOCOUNT ON;
	SELECT PairID FROM KrakenDB.dbo.AssetPairs WHERE dbo.AssetPairs.AlternativePairName = @AlternativeName
END
GO
/****** Object:  StoredProcedure [dbo].[updatePairs]    Script Date: 9/25/2022 4:06:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[updatePairs]
	@AlternativePairName	varchar(50),
	@PairDecimals	int,
	@LotDecimals	int,
	@LotMultiplier	int,
	@MarginCall	int,
	@MarginStop	int,
	@OrderMinimum	decimal(38, 8)
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    UPDATE dbo.AssetPairs
	SET PairDecimals = @PairDecimals,
		LotDecimals = @LotDecimals,
		LotMultiplier = @LotMultiplier,
		MarginCall = @MarginCall,
		MarginStop = @MarginStop,
		OrderMinimum = @OrderMinimum
	WHERE dbo.AssetPairs.AlternativePairName = @AlternativePairName
END
GO
USE [master]
GO
ALTER DATABASE [KrakenDB] SET  READ_WRITE 
GO