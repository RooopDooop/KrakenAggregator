package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/go-redis/redis"
)

type pairData struct {
	PairID          int
	AlternativeName string
}

func watchTicker(client *redis.Client) {
	if _, err := client.Pipelined(func(rdb redis.Pipeliner) error {
		for _, strAssetPair := range fetchAssetsPairs(client) {
			var formattedPair string = strings.Split(strAssetPair, ":")[1]

			fmt.Println("Processing Ticker: " + formattedPair)

			resp, err := http.Get("https://api.kraken.com/0/public/Ticker?pair=" + formattedPair)
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

				rdb.HSet("Ticker:"+formattedPair+"#"+strconv.Itoa(int(time.Now().Unix())), "AskingPrice", aPrice)
				rdb.HSet("Ticker:"+formattedPair+"#"+strconv.Itoa(int(time.Now().Unix())), "AskingWholeLotVolume", aWholeLotVolume)
				rdb.HSet("Ticker:"+formattedPair+"#"+strconv.Itoa(int(time.Now().Unix())), "AskingLotVolume", aLotVolume)

				rdb.HSet("Ticker:"+formattedPair+"#"+strconv.Itoa(int(time.Now().Unix())), "BidPrice", bPrice)
				rdb.HSet("Ticker:"+formattedPair+"#"+strconv.Itoa(int(time.Now().Unix())), "BidWholeLotVolume", bWholeLotVolume)
				rdb.HSet("Ticker:"+formattedPair+"#"+strconv.Itoa(int(time.Now().Unix())), "BidLotVolume", bLotVolume)

				rdb.HSet("Ticker:"+formattedPair+"#"+strconv.Itoa(int(time.Now().Unix())), "LatestTradePrice", lastTradePrice)
				rdb.HSet("Ticker:"+formattedPair+"#"+strconv.Itoa(int(time.Now().Unix())), "TwentyFourLotVolume", lastTwentyFourLotVolume)

				rdb.HSet("Ticker:"+formattedPair+"#"+strconv.Itoa(int(time.Now().Unix())), "TodayTradeVolume", todayTradeVolume)
				rdb.HSet("Ticker:"+formattedPair+"#"+strconv.Itoa(int(time.Now().Unix())), "TwentyFourTradeVolume", lasttwentyFourTradeVolume)

				rdb.HSet("Ticker:"+formattedPair+"#"+strconv.Itoa(int(time.Now().Unix())), "TodayWeightedAvgPrice", todayWeightedAvgPrice)
				rdb.HSet("Ticker:"+formattedPair+"#"+strconv.Itoa(int(time.Now().Unix())), "TwentyFourWeightedAvgPrice", lasttwentyFourWeightedAvgPrice)

				rdb.HSet("Ticker:"+formattedPair+"#"+strconv.Itoa(int(time.Now().Unix())), "TodayTradeQuantity", todayTradeQuantity)
				rdb.HSet("Ticker:"+formattedPair+"#"+strconv.Itoa(int(time.Now().Unix())), "TwentyFourTradeQuantity", lastTwentyFourTradeQuantity)

				rdb.HSet("Ticker:"+formattedPair+"#"+strconv.Itoa(int(time.Now().Unix())), "TodayLow", todayLow)
				rdb.HSet("Ticker:"+formattedPair+"#"+strconv.Itoa(int(time.Now().Unix())), "TwentyFourLow", lasttwentyFourLow)

				rdb.HSet("Ticker:"+formattedPair+"#"+strconv.Itoa(int(time.Now().Unix())), "TodayHigh", todayHigh)
				rdb.HSet("Ticker:"+formattedPair+"#"+strconv.Itoa(int(time.Now().Unix())), "TwentyFourHigh", lasttwentyFourHigh)

				rdb.HSet("Ticker:"+formattedPair+"#"+strconv.Itoa(int(time.Now().Unix())), "OpeningPrice", todayOpeningPrice)
			}
		}
		return nil
	}); err != nil {
		panic(err)
	}

}
