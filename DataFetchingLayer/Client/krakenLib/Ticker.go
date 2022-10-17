package krakenLib

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

	redisLib "J.Morin/KrakenScraper/redisLib"
)

func ProcessTicker(client *redis.Client, URL string) {
	var PairName string = strings.Split(URL, "?pair=")[1]

	resp, err := http.Get(URL)
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

		client.HSet("Ticker:"+PairName+"#"+strconv.Itoa(int(time.Now().Unix())), "AskingPrice", aPrice)
		client.HSet("Ticker:"+PairName+"#"+strconv.Itoa(int(time.Now().Unix())), "AskingWholeLotVolume", aWholeLotVolume)
		client.HSet("Ticker:"+PairName+"#"+strconv.Itoa(int(time.Now().Unix())), "AskingLotVolume", aLotVolume)

		client.HSet("Ticker:"+PairName+"#"+strconv.Itoa(int(time.Now().Unix())), "BidPrice", bPrice)
		client.HSet("Ticker:"+PairName+"#"+strconv.Itoa(int(time.Now().Unix())), "BidWholeLotVolume", bWholeLotVolume)
		client.HSet("Ticker:"+PairName+"#"+strconv.Itoa(int(time.Now().Unix())), "BidLotVolume", bLotVolume)

		client.HSet("Ticker:"+PairName+"#"+strconv.Itoa(int(time.Now().Unix())), "LatestTradePrice", lastTradePrice)
		client.HSet("Ticker:"+PairName+"#"+strconv.Itoa(int(time.Now().Unix())), "TwentyFourLotVolume", lastTwentyFourLotVolume)

		client.HSet("Ticker:"+PairName+"#"+strconv.Itoa(int(time.Now().Unix())), "TodayTradeVolume", todayTradeVolume)
		client.HSet("Ticker:"+PairName+"#"+strconv.Itoa(int(time.Now().Unix())), "TwentyFourTradeVolume", lasttwentyFourTradeVolume)

		client.HSet("Ticker:"+PairName+"#"+strconv.Itoa(int(time.Now().Unix())), "TodayWeightedAvgPrice", todayWeightedAvgPrice)
		client.HSet("Ticker:"+PairName+"#"+strconv.Itoa(int(time.Now().Unix())), "TwentyFourWeightedAvgPrice", lasttwentyFourWeightedAvgPrice)

		client.HSet("Ticker:"+PairName+"#"+strconv.Itoa(int(time.Now().Unix())), "TodayTradeQuantity", todayTradeQuantity)
		client.HSet("Ticker:"+PairName+"#"+strconv.Itoa(int(time.Now().Unix())), "TwentyFourTradeQuantity", lastTwentyFourTradeQuantity)

		client.HSet("Ticker:"+PairName+"#"+strconv.Itoa(int(time.Now().Unix())), "TodayLow", todayLow)
		client.HSet("Ticker:"+PairName+"#"+strconv.Itoa(int(time.Now().Unix())), "TwentyFourLow", lasttwentyFourLow)

		client.HSet("Ticker:"+PairName+"#"+strconv.Itoa(int(time.Now().Unix())), "TodayHigh", todayHigh)
		client.HSet("Ticker:"+PairName+"#"+strconv.Itoa(int(time.Now().Unix())), "TwentyFourHigh", lasttwentyFourHigh)

		client.HSet("Ticker:"+PairName+"#"+strconv.Itoa(int(time.Now().Unix())), "OpeningPrice", todayOpeningPrice)
	}

	fmt.Println("Processed ticker: " + PairName)
	redisLib.DisconnectFromRedis(client)
}
