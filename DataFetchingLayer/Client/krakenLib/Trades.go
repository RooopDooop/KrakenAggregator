package krakenLib

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strings"

	"J.Morin/KrakenScraper/redisLib"
	"github.com/go-redis/redis"
)

type TradeObject struct {
	PairID        int    `json:"PairID"`
	Price         string `json:"Price"`
	Volume        string `json:"Volume"`
	Time          string `json:"tradeTime"`
	BuyOrSell     string `json:"BuyOrSell"`
	MarketOrLimit string `json:"MarketOrLimit"`
}

func ProcessTrades(client *redis.Client, URL string) {
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

	if response["result"] != nil {
		for key, arrTrades := range response["result"].(map[string]interface{}) {
			if key != "last" {
				for _, objTrade := range arrTrades.([]interface{}) {
					client.HSet("Trade:"+PairName+"#"+fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)), "Price", objTrade.([]interface{})[0].(string))
					client.HSet("Trade:"+PairName+"#"+fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)), "Volume", objTrade.([]interface{})[1].(string))

					if objTrade.([]interface{})[3].(string) == "b" {
						client.HSet("Trade:"+PairName+"#"+fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)), "BuyOrSell", "Buy")
					} else if objTrade.([]interface{})[3].(string) == "s" {
						client.HSet("Trade:"+PairName+"#"+fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)), "BuyOrSell", "Sell")
					}

					if objTrade.([]interface{})[4].(string) == "l" {
						client.HSet("Trade:"+PairName+"#"+fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)), "MarketOrLimit", "Limit")
					} else if objTrade.([]interface{})[4].(string) == "m" {
						client.HSet("Trade:"+PairName+"#"+fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)), "MarketOrLimit", "Market")
					}
				}
			}
		}
	}

	fmt.Println("Processed trade: " + PairName)
	redisLib.DisconnectFromRedis(client)
}
