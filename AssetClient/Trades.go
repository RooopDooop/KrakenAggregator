package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"

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

func watchTrades(client *redis.Client, strPair string) {
	fmt.Println("Processing Trades: " + strPair)
	requestProxy()

	resp, err := http.Get("https://api.kraken.com/0/public/Trades?pair=" + strPair)
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
					client.HSet("Trade:"+strPair+"#"+fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)), "Price", objTrade.([]interface{})[0].(string))
					client.HSet("Trade:"+strPair+"#"+fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)), "Volume", objTrade.([]interface{})[1].(string))

					if objTrade.([]interface{})[3].(string) == "b" {
						client.HSet("Trade:"+strPair+"#"+fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)), "BuyOrSell", "Buy")
					} else if objTrade.([]interface{})[3].(string) == "s" {
						client.HSet("Trade:"+strPair+"#"+fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)), "BuyOrSell", "Sell")
					}

					if objTrade.([]interface{})[4].(string) == "l" {
						client.HSet("Trade:"+strPair+"#"+fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)), "MarketOrLimit", "Limit")
					} else if objTrade.([]interface{})[4].(string) == "m" {
						client.HSet("Trade:"+strPair+"#"+fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)), "MarketOrLimit", "Market")
					}
				}
			}
		}
	}

	fmt.Println("Inserted all trades")
}
