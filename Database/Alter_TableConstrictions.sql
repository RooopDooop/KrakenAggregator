USE KrakenDB

SET IDENTITY_INSERT [dbo].[WBMessageLog] OFF
GO
ALTER TABLE [dbo].[AssetPairs]  WITH CHECK ADD FOREIGN KEY([BaseID])
REFERENCES [dbo].[Assets] ([AssetID])
GO
ALTER TABLE [dbo].[AssetPairs]  WITH CHECK ADD FOREIGN KEY([FeeCurrency])
REFERENCES [dbo].[Assets] ([AssetID])
GO
ALTER TABLE [dbo].[AssetPairs]  WITH CHECK ADD FOREIGN KEY([QuoteID])
REFERENCES [dbo].[Assets] ([AssetID])
GO
ALTER TABLE [dbo].[PairTrades]  WITH CHECK ADD FOREIGN KEY([PairID])
REFERENCES [dbo].[AssetPairs] ([PairID])
GO
ALTER TABLE [dbo].[FiatConversionRates]  WITH CHECK ADD FOREIGN KEY([AssetFromID])
REFERENCES [dbo].[Assets] ([AssetID])
GO
ALTER TABLE [dbo].[FiatConversionRates]  WITH CHECK ADD FOREIGN KEY([AssetToID])
REFERENCES [dbo].[Assets] ([AssetID])
GO
ALTER TABLE [dbo].[PairOHLC]  WITH CHECK ADD FOREIGN KEY([PairID])
REFERENCES [dbo].[AssetPairs] ([PairID])
GO
ALTER TABLE [dbo].[PairTickers]  WITH CHECK ADD FOREIGN KEY([PairID])
REFERENCES [dbo].[AssetPairs] ([PairID])
GO
ALTER TABLE [dbo].[PairFeeInstances]  WITH CHECK ADD FOREIGN KEY([PairID])
REFERENCES [dbo].[AssetPairs] ([PairID])
GO
ALTER TABLE [dbo].[PairLeverageInstances]  WITH CHECK ADD FOREIGN KEY([PairID])
REFERENCES [dbo].[AssetPairs] ([PairID])