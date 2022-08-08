package main

import (
	"fmt"
	"strconv"
	"strings"

	"github.com/go-redis/redis"
)

func determineFiat(strAlternativeName string) string {
	switch strAlternativeName {
	case "GBP", "GBP.HOLD", "AUD", "AUD.HOLD", "EUR", "EUR.HOLD", "CAD", "CAD.HOLD", "USD", "USD.HOLD", "CHF", "CHF.HOLD", "JPY":
		return "true"
	}

	return "false"
}

func determineCollateral(collateralValue map[string]interface{}) string {
	if collateralValue["collateral_value"] != nil {
		collateralValue := fmt.Sprintf("%v", collateralValue["collateral_value"].(float64))

		return collateralValue
	}

	return "N/A"
}

func fetchFiatAssets(client *redis.Client) []string {
	var fiatAsset []string = []string{}

	val, err := client.Do("KEYS", "*Asset:*").Result()
	if err != nil {
		if err == redis.Nil {
			fmt.Println("key does not exists, skipping...")
		}
		panic(err)
	}

	for _, interAsset := range val.([]interface{}) {
		val, err := client.HGet(interAsset.(string), "Fiat").Result()
		if err != nil {
			panic(err)
		}

		valBool, boolErr := strconv.ParseBool(val)
		if boolErr != nil {
			panic(boolErr)
		}

		if valBool && len(strings.Split(interAsset.(string), ":")[1]) == 3 {
			fiatAsset = append(fiatAsset, interAsset.(string))
		}

	}

	return fiatAsset
}

func fetchAssetsPairs(client *redis.Client) []string {
	var AssetPairs []string = []string{}

	val, err := client.Do("KEYS", "*AssetPair:*").Result()
	if err != nil {
		if err == redis.Nil {
			fmt.Println("key does not exists, skipping...")
		}
		panic(err)
	}

	for _, interPair := range val.([]interface{}) {
		AssetPairs = append(AssetPairs, interPair.(string))
	}

	return AssetPairs
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
