package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
)

type OHCLObject struct {
	PairID     int    `json:"PairID"`
	EpochTime  int    `json:"epochTime"`
	PriceOpen  string `json:"priceOpen"`
	PriceHigh  string `json:"priceHigh"`
	PriceLow   string `json:"priceLow"`
	PriceClose string `json:"priceClose"`
	VWAP       string `json:"VWAP"`
	Volume     string `json:"volume"`
	Count      int    `json:"count"`
}

func watchOHLC() {
	var arrOHCLObject []OHCLObject = []OHCLObject{}

	for _, objPairData := range getPairs() {
		fmt.Println("Pulling OHLC data for: " + objPairData.AlternativeName)
		resp, err := http.Get("https://api.kraken.com/0/public/OHLC?pair=" + objPairData.AlternativeName + "&interval=1")
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
			for headerStr, objResult := range response["result"].(map[string]interface{}) {
				if headerStr != "last" {
					for _, OHLCData := range objResult.([]interface{}) {
						var OHCLInstance OHCLObject = OHCLObject{
							PairID:     objPairData.PairID,
							EpochTime:  int(OHLCData.([]interface{})[0].(float64)),
							PriceOpen:  OHLCData.([]interface{})[1].(string),
							PriceHigh:  OHLCData.([]interface{})[2].(string),
							PriceLow:   OHLCData.([]interface{})[3].(string),
							PriceClose: OHLCData.([]interface{})[4].(string),
							VWAP:       OHLCData.([]interface{})[5].(string),
							Volume:     OHLCData.([]interface{})[6].(string),
							Count:      int(OHLCData.([]interface{})[7].(float64)),
						}

						arrOHCLObject = append(arrOHCLObject, OHCLInstance)
					}
				}
			}
		} else {
			fmt.Println("INVALID OHLC, skipping: " + objPairData.AlternativeName)
		}
	}

	jsonData, err := json.Marshal(arrOHCLObject)
	if err != nil {
		fmt.Printf("Error: %s", err)
		return
	}

	sqlInsertOHLC := "EXEC [KrakenDB].[dbo].[insertOHLC] @JSONData = '" + string(jsonData) + "'"
	_, errInsertOHLC := deb.Exec(sqlInsertOHLC)

	if errInsertOHLC != nil {
		panic(errInsertOHLC)
	}

	fmt.Println("Inserts Completed - Completed CRON job")
}
