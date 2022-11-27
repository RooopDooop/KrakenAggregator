package krakenLib

import (
	"context"
	"crypto/sha256"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strings"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
)

type Ticker struct {
	AskingPrice          string
	AskingWholeLotVolume string
	AskingLotVolume      string

	BuyPrice          string
	BuyWholeLotVolume string
	BuyLotVolume      string

	LastTradePrice  string
	LastTradeVolume string

	VolumeToday          string
	VolumeLastTwentyFour string

	VolumeWeightedToday          string
	VolumeWeightedLastTwentyFour string

	TradeQuantity               float64
	TradeQuantityLastTwentyFour float64

	LowToday          string
	LowLastTwentyFour string

	HighToday          string
	HighLastTwentyFour string

	OpeningPrice string
}

func ProcessTicker(mongoClient *mongo.Client, URL string) {
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

	TickerCollections := mongoClient.Database("KrakenDB").Collection("Tickers")
	for _, objResult := range response["result"].(map[string]interface{}) {
		shaEncoded := sha256.Sum256([]byte(fmt.Sprintf("%v", objResult)))
		objTicker := bson.M{
			"_id":                  shaEncoded,
			"AlternativePairName":  PairName,
			"AskingPrice":          objResult.(map[string]interface{})["a"].([]interface{})[0].(string),
			"AskingWholeLotVolume": objResult.(map[string]interface{})["a"].([]interface{})[1].(string),
			"AskingLotVolume":      objResult.(map[string]interface{})["a"].([]interface{})[2].(string),

			"BuyPrice":          objResult.(map[string]interface{})["b"].([]interface{})[0].(string),
			"BuyWholeLotVolume": objResult.(map[string]interface{})["b"].([]interface{})[1].(string),
			"BuyLotVolume":      objResult.(map[string]interface{})["b"].([]interface{})[2].(string),

			"LastTradePrice":  objResult.(map[string]interface{})["c"].([]interface{})[0].(string),
			"LastTradeVolume": objResult.(map[string]interface{})["c"].([]interface{})[0].(string),

			"VolumeToday":          objResult.(map[string]interface{})["v"].([]interface{})[0].(string),
			"VolumeLastTwentyFour": objResult.(map[string]interface{})["v"].([]interface{})[1].(string),

			"VolumeWeightedToday":          objResult.(map[string]interface{})["p"].([]interface{})[0].(string),
			"VolumeWeightedLastTwentyFour": objResult.(map[string]interface{})["p"].([]interface{})[1].(string),

			"TradeQuantity":               objResult.(map[string]interface{})["t"].([]interface{})[0].(float64),
			"TradeQuantityLastTwentyFour": objResult.(map[string]interface{})["t"].([]interface{})[1].(float64),

			"LowToday":          objResult.(map[string]interface{})["l"].([]interface{})[0].(string),
			"LowLastTwentyFour": objResult.(map[string]interface{})["l"].([]interface{})[1].(string),

			"HighToday":          objResult.(map[string]interface{})["h"].([]interface{})[0].(string),
			"HighLastTwentyFour": objResult.(map[string]interface{})["h"].([]interface{})[1].(string),

			"OpeningPrice": objResult.(map[string]interface{})["o"].(string),
		}

		_, errInsert := TickerCollections.InsertOne(context.Background(), objTicker)
		if errInsert != nil {
			if strings.Split(errInsert.Error(), ":")[0] != "write exception" {
				panic(errInsert)
			}
		}
	}

	fmt.Println("Ticker processed: " + PairName + " - " + URL)
}
