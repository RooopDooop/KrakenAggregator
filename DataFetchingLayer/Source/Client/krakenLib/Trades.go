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

func ProcessTrades(mongoClient *mongo.Client, URL string) {
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

	TradesCollections := mongoClient.Database("KrakenDB").Collection("Trades")
	if response["result"] != nil {
		for key, arrRawTrades := range response["result"].(map[string]interface{}) {
			if key != "last" {
				for _, objTrade := range arrRawTrades.([]interface{}) {
					var BuyOrSell string
					if objTrade.([]interface{})[3].(string) == "b" {
						BuyOrSell = "Buy"
					} else if objTrade.([]interface{})[3].(string) == "s" {
						BuyOrSell = "Sell"
					}

					var MarketOrLimit string
					if objTrade.([]interface{})[4].(string) == "l" {
						MarketOrLimit = "Limit"
					} else if objTrade.([]interface{})[4].(string) == "m" {
						MarketOrLimit = "Market"
					}

					shaEncoded := sha256.Sum256([]byte(fmt.Sprintf("%v", objTrade)))
					objTrade := bson.M{
						"_id":                 shaEncoded,
						"AlternativePairName": PairName,
						"Price":               objTrade.([]interface{})[0].(string),
						"Volume":              objTrade.([]interface{})[1].(string),
						"Time":                fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)),
						"BuyOrSell":           BuyOrSell,
						"MarketOrLimit":       MarketOrLimit,
					}

					_, errInsert := TradesCollections.InsertOne(context.Background(), objTrade)
					if errInsert != nil {
						if strings.Split(errInsert.Error(), ":")[0] != "write exception" {
							panic(errInsert)
						}
					}
				}
			}
		}
	}

	fmt.Println("Trade processed: " + PairName + " - " + URL)
}
