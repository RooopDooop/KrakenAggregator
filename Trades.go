package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strings"

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

func watchTrades(client *redis.Client) {
	if _, err := client.Pipelined(func(rdb redis.Pipeliner) error {
		for _, strAssetPair := range fetchAssetsPairs(client) {
			var formattedPair string = strings.Split(strAssetPair, ":")[1]

			fmt.Println("Processing trades: " + formattedPair)

			resp, err := http.Get("https://api.kraken.com/0/public/Trades?pair=" + formattedPair)
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
							rdb.HSet("Trade:"+formattedPair+"#"+fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)), "Price", objTrade.([]interface{})[0].(string))
							rdb.HSet("Trade:"+formattedPair+"#"+fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)), "Volume", objTrade.([]interface{})[1].(string))

							if objTrade.([]interface{})[3].(string) == "b" {
								rdb.HSet("Trade:"+formattedPair+"#"+fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)), "BuyOrSell", "Buy")
							} else if objTrade.([]interface{})[3].(string) == "s" {
								rdb.HSet("Trade:"+formattedPair+"#"+fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)), "BuyOrSell", "Sell")
							}

							if objTrade.([]interface{})[4].(string) == "l" {
								rdb.HSet("Trade:"+formattedPair+"#"+fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)), "MarketOrLimit", "Limit")
							} else if objTrade.([]interface{})[4].(string) == "m" {
								rdb.HSet("Trade:"+formattedPair+"#"+fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)), "MarketOrLimit", "Market")
							}
						}
					}
				}
			}
		}

		return nil
	}); err != nil {
		panic(err)
	}

	fmt.Println("Inserted all trades")
}
