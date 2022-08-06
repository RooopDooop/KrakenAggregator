package main

import (
	"fmt"
	"strconv"
)

func determineFiat(strAlternativeName string) string {
	switch strAlternativeName {
	case "GBP", "GBP.HOLD", "AUD", "AUD.HOLD", "EUR", "EUR.HOLD", "CAD", "CAD.HOLD", "USD", "USD.HOLD", "CHF", "CHF.HOLD", "JPY":
		return "1"
	}

	return "0"
}

func determineCollateral(collateralValue map[string]interface{}) string {
	if collateralValue["collateral_value"] != nil {
		collateralValue := fmt.Sprintf("%v", collateralValue["collateral_value"].(float64))

		return collateralValue
	}

	return "N/A"
}

func determineIfPairExists(strAlternativePairName string) (bool, *int) {
	sqlFindPair := "SELECT PairID " +
		"FROM [KrakenDB].[dbo].[AssetPairs] " +
		"WHERE [KrakenDB].[dbo].[AssetPairs].[AlternativePairName] = '" + strAlternativePairName + "'"

	var id int = 0

	err := deb.QueryRow(sqlFindPair).Scan(&id)

	if err != nil {
		if err.Error() == "sql: no rows in result set" {
			return false, nil
		}

		panic(err)
	}

	return true, &id
}

func determineIfFeeExists(PairID int, FeeType string, FeeVolume string) (bool, *int) {
	sqlFindPair := "SELECT FeeID " +
		"FROM [KrakenDB].[dbo].[PairFeeInstances] " +
		"WHERE PairID = " + strconv.Itoa(PairID) + " " +
		"AND FeeType = '" + FeeType + "' " +
		"AND FeeVolume = " + FeeVolume

	var id int = 0

	err := deb.QueryRow(sqlFindPair).Scan(&id)

	if err != nil {
		if err.Error() == "sql: no rows in result set" {
			return false, nil
		}

		panic(err)
	}

	return true, &id
}

func determineIfLeverageExists(PairID int, LeverageType string, LeverageValue string) (bool, *int) {
	sqlFindPair := "SELECT [LeverageID] FROM [KrakenDB].[dbo].[PairLeverageInstances] WHERE PairID = " + strconv.Itoa(PairID) + " AND LeverageType = '" + LeverageType + "' AND LeverageValue = " + LeverageValue

	var id int = 0

	err := deb.QueryRow(sqlFindPair).Scan(&id)

	if err != nil {
		if err.Error() == "sql: no rows in result set" {
			return false, nil
		}

		panic(err)
	}

	return true, &id
}

func determineIfAssetExists(strAlternativeName string) (bool, *int) {
	sqlFindAsset := "SELECT AssetID " +
		"FROM [KrakenDB].[dbo].[Assets] " +
		"WHERE [KrakenDB].[dbo].[Assets].AlternativeName = '" + strAlternativeName + "'"

	var id int = 0

	err := deb.QueryRow(sqlFindAsset).Scan(&id)

	if err != nil {
		if err.Error() == "sql: no rows in result set" {
			return false, nil
		}

		panic(err)
	}

	return true, &id
}

/*func containsInt(s []int, target int) bool {
	for _, v := range s {
		if v == target {
			return true
		}
	}

	return false
}

func containsString(s []string, target string) bool {
	for _, v := range s {
		if v == target {
			return true
		}
	}

	return false
}*/
