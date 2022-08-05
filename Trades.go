package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
)

type TradeObject struct {
	PairID        int    `json:"PairID"`
	Price         string `json:"Price"`
	Volume        string `json:"Volume"`
	Time          string `json:"tradeTime"`
	BuyOrSell     string `json:"BuyOrSell"`
	MarketOrLimit string `json:"MarketOrLimit"`
}

func watchTrades() {
	var arrInsertTrades []TradeObject

	for _, objPairData := range getPairs() {
		fmt.Println("Getting trades: " + objPairData.AlternativeName)

		resp, err := http.Get("https://api.kraken.com/0/public/Trades?pair=" + objPairData.AlternativeName)
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
						var objTrade TradeObject = TradeObject{
							PairID:        objPairData.PairID,
							Price:         objTrade.([]interface{})[0].(string),
							Volume:        objTrade.([]interface{})[1].(string),
							Time:          fmt.Sprintf("%f", objTrade.([]interface{})[2].(float64)),
							BuyOrSell:     objTrade.([]interface{})[3].(string),
							MarketOrLimit: objTrade.([]interface{})[4].(string),
						}

						if objTrade.BuyOrSell == "b" {
							objTrade.BuyOrSell = "Buy"
						} else if objTrade.BuyOrSell == "s" {
							objTrade.BuyOrSell = "Sell"
						}

						if objTrade.MarketOrLimit == "l" {
							objTrade.MarketOrLimit = "Limit"
						} else if objTrade.MarketOrLimit == "m" {
							objTrade.MarketOrLimit = "Market"
						}

						arrInsertTrades = append(arrInsertTrades, objTrade)
					}
				}
			}
		}
	}

	jsonInsert, errJson := json.Marshal(arrInsertTrades)

	if errJson != nil {
		panic(errJson)
	}

	sqlInsertTrade := "EXEC [KrakenDB].[dbo].[insertAssetTrade] @JSONData = '" + string(jsonInsert) + "'"

	_, errInsertTrade := deb.Exec(sqlInsertTrade)

	if errInsertTrade != nil {
		panic(errInsertTrade)
	}

	fmt.Println("Inserted all trades")
}
