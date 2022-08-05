package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strconv"
)

type pairData struct {
	PairID          int
	AlternativeName string
}

func watchTicker() {
	for _, objPairData := range getPairs() {
		resp, err := http.Get("https://api.kraken.com/0/public/Ticker?pair=" + objPairData.AlternativeName)
		if err != nil {
			log.Fatalln(err)
		}

		defer resp.Body.Close()

		bodyBytes, bodyErr := ioutil.ReadAll(resp.Body)
		if bodyErr != nil {
			log.Fatalln(bodyErr)
		}

		var response map[string]interface{}

		if errResponse := json.Unmarshal(bodyBytes, &response); errResponse != nil {
			log.Fatal(errResponse)
		}

		for _, objResult := range response["result"].(map[string]interface{}) {
			var aPrice string = objResult.(map[string]interface{})["a"].([]interface{})[0].(string)
			var aWholeLotVolume string = objResult.(map[string]interface{})["a"].([]interface{})[1].(string)
			var aLotVolume string = objResult.(map[string]interface{})["a"].([]interface{})[2].(string)

			var bPrice string = objResult.(map[string]interface{})["b"].([]interface{})[0].(string)
			var bWholeLotVolume string = objResult.(map[string]interface{})["b"].([]interface{})[1].(string)
			var bLotVolume string = objResult.(map[string]interface{})["b"].([]interface{})[2].(string)

			var lastTradePrice string = objResult.(map[string]interface{})["c"].([]interface{})[0].(string)
			var lastTwentyFourLotVolume string = objResult.(map[string]interface{})["c"].([]interface{})[0].(string)

			var todayTradeVolume string = objResult.(map[string]interface{})["v"].([]interface{})[0].(string)
			var lasttwentyFourTradeVolume string = objResult.(map[string]interface{})["v"].([]interface{})[1].(string)

			var todayWeightedAvgPrice string = objResult.(map[string]interface{})["p"].([]interface{})[0].(string)
			var lasttwentyFourWeightedAvgPrice string = objResult.(map[string]interface{})["p"].([]interface{})[1].(string)

			var todayTradeQuantity string = fmt.Sprintf("%v", objResult.(map[string]interface{})["t"].([]interface{})[0].(float64))
			var lastTwentyFourTradeQuantity string = fmt.Sprintf("%v", objResult.(map[string]interface{})["t"].([]interface{})[1].(float64))

			var todayLow string = objResult.(map[string]interface{})["l"].([]interface{})[0].(string)
			var lasttwentyFourLow string = objResult.(map[string]interface{})["l"].([]interface{})[1].(string)

			var todayHigh string = objResult.(map[string]interface{})["h"].([]interface{})[0].(string)
			var lasttwentyFourHigh string = objResult.(map[string]interface{})["h"].([]interface{})[1].(string)

			var todayOpeningPrice string = objResult.(map[string]interface{})["o"].(string)

			sqlInsert := "INSERT INTO [KrakenDB].[dbo].[PairAssetTicker] (TickerDate, PairID, AskingPrice, AskingWholeLotVolume, AskingLotVolume, BidPrice, BidWholeLotVolume, BidLotVolume, LastTradePrice, LastTradeVolume, VolumeToday, VolumeLastTwentyFour, VolumeWeightedToday, VolumeWeightedLastTwentyFour, TradeQuantity, TradeQuantityLastTwentyFour, LowToday, LowLastTwentyFour, HighToday, HighLastTwentyFour, OpeningPrice) " +
				"VALUES (GETDATE(), " + strconv.Itoa(objPairData.PairID) + ", " +
				aPrice +
				", " + aWholeLotVolume +
				", " + aLotVolume +
				", " + bPrice +
				", " + bWholeLotVolume +
				", " + bLotVolume +
				", " + lastTradePrice +
				", " + lastTwentyFourLotVolume +
				", " + todayTradeVolume +
				", " + lasttwentyFourTradeVolume +
				", " + todayWeightedAvgPrice +
				", " + lasttwentyFourWeightedAvgPrice +
				", " + todayTradeQuantity +
				", " + lastTwentyFourTradeQuantity +
				", " + todayLow +
				", " + lasttwentyFourLow +
				", " + todayHigh +
				", " + lasttwentyFourHigh +
				", " + todayOpeningPrice + ")"

			_, errInsert := deb.Exec(sqlInsert)

			if errInsert != nil {
				panic(errInsert)
			}
		}
	}
}

func getPairs() []pairData {
	var arrObjPairData []pairData = []pairData{}

	sqlPairs := "SELECT PairID, AlternativePairName FROM [KrakenDB].[dbo].[AssetPairs] ORDER BY AlternativePairName, PairID"
	rowPairs, errPairs := deb.Query(sqlPairs)

	if errPairs != nil {
		panic(errPairs)
	}

	for rowPairs.Next() {
		var objPair pairData

		scanErr := rowPairs.Scan(
			&objPair.PairID,
			&objPair.AlternativeName,
		)

		if scanErr != nil {
			panic(scanErr)
		}

		arrObjPairData = append(arrObjPairData, objPair)
	}

	return arrObjPairData
}
