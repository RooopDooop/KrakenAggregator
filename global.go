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

	//TODO this is throwing IO timeout. Will need to be more specific with is
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
